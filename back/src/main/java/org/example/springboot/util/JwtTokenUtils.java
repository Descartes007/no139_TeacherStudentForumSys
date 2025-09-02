package org.example.springboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtTokenUtils {

    /**
     * 过期时间 24小时
     */
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    /**
     * 生成token
     * @param userId 用户ID
     * @param secret 密钥(使用用户密码作为密钥)
     * @return JWT token
     */
    public static String genToken(String userId, String secret) {
        return JWT.create()
                .withAudience(userId) // 将userId保存到token里面作为载荷
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 设置过期时间
                .sign(Algorithm.HMAC256(secret)); // 使用用户密码作为密钥
    }
}
