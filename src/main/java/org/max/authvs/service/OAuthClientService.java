package org.max.authvs.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.max.authvs.api.dto.PageVo;
import org.max.authvs.api.dto.client.in.ClientQueryParam;
import org.max.authvs.api.dto.client.in.ClientSaveParam;
import org.max.authvs.api.dto.client.out.ClientSecretResetVo;
import org.max.authvs.api.dto.client.out.ClientVo;
import org.max.authvs.entity.OAuthClient;
import org.max.authvs.mapper.OAuthClientMapper;
import org.max.authvs.security.CustomUserDetails;
import org.max.authvs.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OIDC客户端服务
 */
@Service
@Slf4j
public class OAuthClientService {

    private final OAuthClientMapper clientMapper;
    private final PasswordEncoder passwordEncoder;

    public OAuthClientService(OAuthClientMapper clientMapper, PasswordEncoder passwordEncoder) {
        this.clientMapper = clientMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 分页查询客户端列表
     */
    public PageVo<ClientVo> getClientsByPage(ClientQueryParam queryDTO) {
        Page<OAuthClient> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<OAuthClient> queryWrapper = new LambdaQueryWrapper<>();

        // 添加动态查询条件
        if (StrUtil.isNotBlank(queryDTO.getClientName())) {
            queryWrapper.like(OAuthClient::getClientName, queryDTO.getClientName());
        }
        if (StrUtil.isNotBlank(queryDTO.getClientId())) {
            queryWrapper.like(OAuthClient::getClientId, queryDTO.getClientId());
        }
        if (queryDTO.getClientType() != null) {
            queryWrapper.eq(OAuthClient::getClientType, queryDTO.getClientType());
        }
        if (queryDTO.getEnabled() != null) {
            queryWrapper.eq(OAuthClient::getEnabled, queryDTO.getEnabled());
        }

        queryWrapper.orderByDesc(OAuthClient::getCreatedAt);

        Page<OAuthClient> clientPage = clientMapper.selectPage(page, queryWrapper);

        List<ClientVo> clientVOs = clientPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageVo.<ClientVo>builder()
                .pageNum(clientPage.getCurrent())
                .pageSize(clientPage.getSize())
                .total(clientPage.getTotal())
                .records(clientVOs)
                .build();
    }

    /**
     * 根据ID查询客户端详情
     */
    public ClientVo getClientById(Long id) {
        OAuthClient client = clientMapper.selectById(id);
        if (client == null) {
            throw new IllegalArgumentException("客户端不存在");
        }
        return convertToVO(client);
    }

    /**
     * 创建客户端
     */
    public ClientVo createClient(ClientSaveParam saveDTO) {
        // 生成客户端ID（随机字符串）
        String clientId = generateClientId(saveDTO.getClientName());

        // 检查clientId是否已存在
        LambdaQueryWrapper<OAuthClient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthClient::getClientId, clientId);
        if (clientMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("客户端ID已存在，请重试");
        }

        // 生成客户端密钥（随机字符串）
        String plainSecret = RandomUtil.randomString(32);
        String encodedSecret = passwordEncoder.encode(plainSecret);

        OAuthClient client = new OAuthClient();
        client.setClientId(clientId);
        client.setClientSecret(encodedSecret);
        copyProperties(saveDTO, client);

        // 设置创建人
        try {
            CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                client.setCreatedBy(currentUser.getId());
            }
        } catch (Exception e) {
            log.warn("获取当前用户失败", e);
        }

        client.setEnabled(true);

        clientMapper.insert(client);

        ClientVo vo = convertToVO(client);
        // 创建时返回明文密钥
        vo.setClientSecret(plainSecret);

        log.info("创建客户端成功: clientId={}", clientId);
        return vo;
    }

    /**
     * 更新客户端
     */
    public ClientVo updateClient(Long id, ClientSaveParam saveDTO) {
        OAuthClient client = clientMapper.selectById(id);
        if (client == null) {
            throw new IllegalArgumentException("客户端不存在");
        }

        copyProperties(saveDTO, client);

        // 设置更新人
        try {
            CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                client.setUpdatedBy(currentUser.getId());
            }
        } catch (Exception e) {
            log.warn("获取当前用户失败", e);
        }

        clientMapper.updateById(client);

        log.info("更新客户端成功: id={}, clientId={}", id, client.getClientId());
        return convertToVO(client);
    }

    /**
     * 启用/禁用客户端
     */
    public void toggleClientStatus(Long id, Boolean enabled) {
        OAuthClient client = clientMapper.selectById(id);
        if (client == null) {
            throw new IllegalArgumentException("客户端不存在");
        }

        client.setEnabled(enabled);

        // 设置更新人
        try {
            CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                client.setUpdatedBy(currentUser.getId());
            }
        } catch (Exception e) {
            log.warn("获取当前用户失败", e);
        }

        clientMapper.updateById(client);
        log.info("切换客户端状态成功: id={}, clientId={}, enabled={}", id, client.getClientId(), enabled);
    }

    /**
     * 重置客户端密钥
     */
    public ClientSecretResetVo resetClientSecret(Long id) {
        OAuthClient client = clientMapper.selectById(id);
        if (client == null) {
            throw new IllegalArgumentException("客户端不存在");
        }

        // 生成新的客户端密钥
        String plainSecret = RandomUtil.randomString(32);
        String encodedSecret = passwordEncoder.encode(plainSecret);

        client.setClientSecret(encodedSecret);

        // 设置更新人
        try {
            CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                client.setUpdatedBy(currentUser.getId());
            }
        } catch (Exception e) {
            log.warn("获取当前用户失败", e);
        }

        clientMapper.updateById(client);

        log.info("重置客户端密钥成功: id={}, clientId={}", id, client.getClientId());

        return new ClientSecretResetVo(
                client.getClientId(),
                plainSecret,
                "密钥已重置成功，请妥善保存，此密钥仅显示一次！"
        );
    }

    /**
     * 生成客户端ID
     */
    private String generateClientId(String clientName) {
        // 转换为拼音首字母或使用随机字符串
        String prefix = StrUtil.isNotBlank(clientName) && clientName.length() > 0
                ? clientName.substring(0, Math.min(3, clientName.length())).toLowerCase()
                : "client";
        // 移除非字母数字字符
        prefix = prefix.replaceAll("[^a-zA-Z0-9]", "");
        if (StrUtil.isBlank(prefix)) {
            prefix = "client";
        }
        return prefix + "-" + RandomUtil.randomString(8);
    }

    /**
     * 复制属性
     */
    private void copyProperties(ClientSaveParam param, OAuthClient client) {
        client.setClientName(param.getClientName());
        client.setClientType(param.getClientType());
        client.setGrantTypes(param.getGrantTypes());
        client.setRedirectUris(param.getRedirectUris());
        client.setPostLogoutRedirectUris(param.getPostLogoutRedirectUris());
        client.setAccessTokenValidity(param.getAccessTokenValidity());
        client.setRefreshTokenValidity(param.getRefreshTokenValidity());
        client.setIdTokenValidity(param.getIdTokenValidity());
        client.setScopes(param.getScopes());
        client.setAutoApprove(param.getAutoApprove());
        client.setDescription(param.getDescription());
        client.setLogoUrl(param.getLogoUrl());
        client.setHomeUrl(param.getHomeUrl());
    }

    /**
     * 转换为Vo
     */
    private ClientVo convertToVO(OAuthClient client) {
        ClientVo vo = new ClientVo();
        BeanUtils.copyProperties(client, vo);

        // 脱敏显示密钥：显示前4位+****+后4位
        if (StrUtil.isNotBlank(client.getClientSecret()) && client.getClientSecret().length() > 8) {
            String secret = client.getClientSecret();
            vo.setClientSecret(secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4));
        }

        // 转换逗号分隔的字符串为列表
        vo.setGrantTypes(splitToList(client.getGrantTypes()));
        vo.setRedirectUris(splitToList(client.getRedirectUris()));
        vo.setPostLogoutRedirectUris(splitToList(client.getPostLogoutRedirectUris()));
        vo.setScopes(splitToList(client.getScopes()));

        // 设置客户端类型描述
        vo.setClientTypeDesc(getClientTypeDesc(client.getClientType()));

        return vo;
    }

    /**
     * 字符串分割为列表
     */
    private List<String> splitToList(String str) {
        if (StrUtil.isBlank(str)) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * 获取客户端类型描述
     */
    private String getClientTypeDesc(Integer clientType) {
        if (clientType == null) {
            return "未知";
        }
        switch (clientType) {
            case 1:
                return "Web应用";
            case 2:
                return "移动应用";
            case 3:
                return "单页应用";
            case 4:
                return "服务端应用";
            default:
                return "未知";
        }
    }
}
