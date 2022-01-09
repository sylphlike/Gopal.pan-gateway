package com.github.sylphlike.gateway.filter.handler;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.sylphlike.framework.adapt.JsonConfig;
import com.github.sylphlike.framework.norm.Response;
import com.github.sylphlike.framework.norm.util.CharsetUtil;
import com.github.sylphlike.framework.security.JWTToken;
import com.github.sylphlike.framework.security.RSAEncrypt;
import com.github.sylphlike.gateway.common.config.GConstants;
import com.github.sylphlike.gateway.common.domain.EncryptApproveBO;
import com.github.sylphlike.gateway.common.domain.PassCheckVO;
import com.github.sylphlike.gateway.common.enums.ApproveType;
import com.github.sylphlike.gateway.common.enums.GReply;
import com.github.sylphlike.gateway.common.enums.ParamsType;
import com.github.sylphlike.gateway.common.exception.GatewayException;
import com.github.sylphlike.gateway.common.utils.ParamUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * <p>  time 05/11/2020 11:25  星期四 【dd/MM/YYYY HH:mm】 </p>
 * <p> email 15923508369@163.com </p>
 * @author Gopal.pan
 * @version 1.0.0
 */
@Service
public class PermissionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionHandler.class);

    private static RedissonClient redissonClient;


    public PermissionHandler(RedissonClient redissonClient) {
        PermissionHandler.redissonClient = redissonClient;
    }

    /**
     * 获取授权认证类型
     * <p>  time 11:27 2020/11/12 【HH:mm yyyy/MM/dd】  </p>
     * <p> email 15923508369@163.com </p>
     * @param path  请求地址路径
     * @return   com.horse.gateway.common.enums.ApproveType
     * @author   Gopal.pan
     */
    public static ApproveType requestType(String path){
        for (String url : GConstants.NO_VERIFICATION_API_URL) {
            if(path.endsWith(url)){
                return ApproveType.NONE;
            }
        }
        String approveType = path.split("/")[2];
        ApproveType anEnum = ApproveType.getEnum(approveType);
        Optional.ofNullable(anEnum).orElseThrow(()-> new GatewayException(GReply.GATEWAY_INVALID_ADDRESS));
        return anEnum;
    }

    /**
     * 根据请求地址Path校验权限
     *  地址规则: /应用上下文地址/权限类型/业务模块/业务功能
     *  权限类型分为三类：
     *    1 无需任何权限(NONE n)   适用于网站在用户不需要登录就可以访问的接口
     *    2 登录授权(LOGIN l)     适用于网站在用户需要登录才可以访问的接口
     *    3 角色资源权限(ROLE r)   适用于后台管理系统需要用户登录并且授予资源访问权限接口
     *    4 加密授权(ENCRYPT e)   适用于接口对企业用户开放接口
     *    5 state(STATE s)      适用于开放平台登录接口回调认证
     *    /dictionary-service/n/system/page/list,权限类型为无需任务权限接口
     * <p>  time 11:39 2020/11/12 【HH:mm yyyy/MM/dd】  </p>
     * <p> email 15923508369@163.com </p>
     * @param approveType           认证类型
     * @param path                  资源地址路径（/应用上下文地址/权限类型/业务模块/业务功能）
     * @param originParams          原始请求参数
     * @param paramsType            原始参数类型
     * @param token                 令牌
     * @return   com.horse.framework.norm.REnums
     * @author   Gopal.pan
     */
    public static Response<PassCheckVO> implement(ApproveType approveType, String path, String originParams, ParamsType paramsType, String token) {

        switch (approveType){
            case NONE:{
                LOGGER.info("【unite-gateway】认证授权校验,无需任何权限");
                return new Response<>(PassCheckVO.builder().params(originParams).build());
            }
            case LOGIN:{
                LOGGER.info("【unite-gateway】认证授权校验,登录授权,token[{}]",token);
                return login(token,originParams);
            }
            case ROLE:{
                LOGGER.info("【unite-gateway】认证授权校验,角色资源权限,token[{}]",token);
                return role(token,path,originParams);
            }
            case ENCRYPT:{
                LOGGER.info("【unite-gateway】认证授权校验,加密授权,参数类型为[{}]",paramsType);
                return encrypt(originParams,path, paramsType);
            }
            case STATE:{
                LOGGER.info("【unite-gateway】认证授权校验,开放平台登录接口回调认证,参数类型为[{}]",paramsType);
                return state(originParams, paramsType);
            }

            default:{
                return Response.error( GReply.GATEWAY_INVALID_ADDRESS);
            }
        }
    }


    /**
     * 登录授权认证
     * <p>  time 9:20 2020/11/13 【HH:mm yyyy/MM/dd】  </p>
     * <p> email 15923508369@163.com </p>
     * @param token         令牌
     * @param originParams  原始请求参数
     * @return   com.horse.framework.norm.Response<java.lang.String>
     * @author   Gopal.pan
     */
    private static Response<PassCheckVO> login(String token, String originParams) {
        try {
            if(StringUtils.isEmpty(token)){
                return Response.error(GReply.GATEWAY_TOKEN_LOST);
            }
            String subject = JWTToken.validateSubject(token);
            if(StringUtils.isEmpty(subject)){
                return Response.error(GReply.GATEWAY_TOKEN_AUTH_FAIL);
            }
            LOGGER.info("【unite-gateway】认证授权校验,登录授权,identityId[{}]",subject);
            return new Response<>(PassCheckVO.builder().identity(subject).params(originParams).build());
        }catch (JWTDecodeException e){
            LOGGER.error("【unite-gateway】认证授权校验,登录授权认证,token无效");
            return Response.error(GReply.GATEWAY_TOKEN_INVALID);
        }catch (TokenExpiredException e) {
            LOGGER.error("【unite-gateway】认证授权校验,登录授权认证,令牌过期[{}]",e.getMessage());
            return Response.error(GReply.GATEWAY_TOKEN_HAS_EXPIRED);
        } catch (Exception e){
            LOGGER.error("【unite-gateway】认证授权校验,登录授权认证,系统异常", e);
            return Response.error(GReply.GATEWAY_TOKEN_AUTH_FAIL);
        }


    }


    /**
     * 角色资源权限认证
     * <p>  time 9:20 2020/11/13 【HH:mm yyyy/MM/dd】  </p>
     * <p> email 15923508369@163.com </p>
     * @param token         令牌
     * @param path          访问路径
     * @param originParams  原始请求参数
     * @return   com.horse.framework.norm.Response<java.lang.String>
     * @author   Gopal.pan
     */
    private static Response<PassCheckVO> role(String token, String path, String originParams) {
        try {
            if(StringUtils.isEmpty(token)){
                return Response.error(GReply.GATEWAY_TOKEN_LOST);
            }
            String subject = JWTToken.validateSubject(token);
            if(StringUtils.isEmpty(subject)){
                return Response.error(GReply.GATEWAY_TOKEN_AUTH_FAIL);
            }

            int index = StringUtils.ordinalIndexOf(path, "/", 2);
            String roleCacheKey = StringUtils.join(appId(path), CharsetUtil.CHAR_ENGLISH_COLON,"auth:",subject);

            RSet<Object> set = redissonClient.getSet(roleCacheKey);
            Boolean member = set.contains(path.substring(index));
            if(ObjectUtils.isEmpty(member) || !member){
                return Response.error(GReply.GATEWAY_NO_ACCESS);
            }
            LOGGER.info("【unite-gateway】认证授权校验,角色资源权限,identityId[{}]",subject);
            return new Response<>(PassCheckVO.builder().identity(subject).params(originParams).build());

        }catch (JWTDecodeException e){
            LOGGER.error("【unite-gateway】认证授权校验,角色资源权限,token无效");
            return Response.error(GReply.GATEWAY_TOKEN_INVALID);
        }catch (TokenExpiredException e) {
            LOGGER.error("【unite-gateway】认证授权校验,角色资源权限,令牌过期[{}]",e.getMessage());
            return Response.error(GReply.GATEWAY_TOKEN_HAS_EXPIRED);
        } catch (Exception e){
            LOGGER.error("【unite-gateway】认证授权校验,角色资源权限,系统异常", e);
            return Response.error(GReply.GATEWAY_TOKEN_AUTH_FAIL);
        }
    }




    /**
     * 参数加密认证
     *  参数格式为
     *      {
     *          "merchantId":"M10001",
     *          "version":"1.0.0",
     *          "data":"加密后的业务参数",
     *          "timestamp":"2020-11-13 12:12:12"
     *      }
     * <p>  time 9:32 2020/11/13 【HH:mm yyyy/MM/dd】  </p>
     * <p> email 15923508369@163.com </p>
     * @param originParams      原始请求参数
     * @param path              访问路径
     * @param paramsType        原始请求参数数据格式类型
     * @return   com.horse.framework.norm.Response<java.lang.String>
     * @author   Gopal.pan
     */
    private static Response<PassCheckVO> encrypt(String originParams, String path, ParamsType paramsType) {

        try {
            if(StringUtils.isEmpty(originParams) || null == paramsType){
                return Response.error(GReply.GATEWAY_DECRYPTION_FAILED);
            }
            EncryptApproveBO encryptApproveBO;
            if(paramsType.equals(ParamsType.FORM_DATA)){
                encryptApproveBO =  JsonConfig.mapper().readValue( ParamUtils.paramFormatJson(originParams),EncryptApproveBO.class);
            }else {
                encryptApproveBO = JsonConfig.mapper().readValue(originParams,EncryptApproveBO.class);
            }
            String merchantId = encryptApproveBO.getMerchantId();
            String cacheKey = StringUtils.join(appId(path), CharsetUtil.CHAR_ENGLISH_COLON,"permission:",merchantId);
            RBucket<String> bucket = redissonClient.getBucket(cacheKey, new StringCodec());
            String privateKey = bucket.get();
            String plaintext = RSAEncrypt.decryptByPrivateKey(encryptApproveBO.getData(), privateKey);

            LOGGER.info("【unite-gateway】认证授权校验,加密授权,identityId[{}]",merchantId);
            return new Response<>(PassCheckVO.builder().identity(merchantId).params(plaintext).build());
        }catch (JsonProcessingException e ){
            LOGGER.error("【unite-gateway】认证授权校验,加密授权,参数格式化异常[{}]", e.getMessage());
            return Response.error(GReply.GATEWAY_DECRYPTION_WRONG_FORMAT);
        } catch (Exception e){
            LOGGER.error("【unite-gateway】认证授权校验,加密授权,系统异常", e);
            return Response.error(GReply.GATEWAY_DECRYPTION_FAILED);
        }
    }


    private static Response<PassCheckVO> state(String originParams, ParamsType paramsType) {
        return new Response<>();
    }






    private static String appId(String path) {
        return path.split("/")[1];
    }

}
