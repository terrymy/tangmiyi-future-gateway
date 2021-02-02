package com.tangmiyi.future.gateway.consts;

/**
 * 全局常量类
 */
public class GatewayConstant {

    /**
     * actuator路径
     */
    public final static String REQUEST_PATH_ACTUATOR = "/actuator/**";

    /**
     * open路径
     */
    public final static String REQUEST_PATH_OPEN = "/**/open/**";

    /**
     * swagger地址
     */
    public final static String SWAGGERDO_PATH_DOC = "/**/v3/api-docs";

    /**
     * manager登录路径
     */
    public final static String MANAGER_PATH_LOGIN = "/v1/manager/login";

    /**
     * 支持地址栏带token
     */
    public final static String ADDR_AUTHORIZATION = "Authorization";

    /**
     * token来源
     */
    public final static String AUDIENCE_MANAGER = "manager";

    /**
     * 签名sign参数
     */
    public final static String REQUEST_PARAMETER_SIGN = "sign";

    /**
     * 签名stamp参数
     */
    public final static String REQUEST_PARAMETER_STAMP = "stamp";

    /**
     * 签名nonce参数
     */
    public final static String REQUEST_PARAMETER_NONCE = "nonce";

    /**
     * 环境test
     */
    public final static String ENV_DEV = "dev";

    /**
     * 环境uat
     */
    public final static String ENV_UAT = "uat";

    /**
     * 环境prod
     */
    public final static String ENV_PROD = "prod";
}
