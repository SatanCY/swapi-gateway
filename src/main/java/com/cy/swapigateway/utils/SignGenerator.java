package com.cy.swapigateway.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * 签名工具
 *
 * @Author：SatanCY
 * @Date：2024/10/1 19:48
 */
public class SignGenerator {

    /**
     * 生成签名
     * @param body 包含需要签名的参数的哈希映射
     * @param secretKey 密钥
     * @return 生成的签名字符串
     */
    public static String generateSign(String body, String secretKey) {
        // 使用SHA256算法的Digester
        Digester md5 = new Digester(DigestAlgorithm.SHA256);
        // 构建签名内容，将哈希映射转换为字符串并拼接密钥
        // 计算签名的摘要并返回摘要的十六进制表示形式
        String digestHex = md5.digestHex(body + "." + secretKey);
        return digestHex;
    }
}
