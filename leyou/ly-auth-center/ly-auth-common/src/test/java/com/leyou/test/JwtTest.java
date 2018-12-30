package com.leyou.test;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {
    private static String PUBLIC_KEY_PATH="D:\\ideaWorkPlace\\leyou-city\\rsa.pub";
    private static String PRIVATE_KEY_PATH="D:\\ideaWorkPlace\\leyou-city\\rsa.pri";
    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * 生成公钥和私钥
     */
    @Test
    public void testRsa() throws Exception {
        //根据密文"234"，生存rsa公钥和私钥,并写入指定文件
        RsaUtils.generateKey(PUBLIC_KEY_PATH, PRIVATE_KEY_PATH, "234");
    }

    /**
     * 读取公钥和私钥
     */
    @Before
    public void testGetRsa() throws Exception {
        //从文件中读取公钥
        this.publicKey = RsaUtils.getPublicKey(PUBLIC_KEY_PATH);
        //从文件中读取私钥
        this.privateKey = RsaUtils.getPrivateKey(PRIVATE_KEY_PATH);
    }

    /**
     * 生成token
     */
    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(1L, "Lee34"), privateKey, 5);
        System.out.println("token = " + token);
    }

    /**
     * 解析token
     */
    @Test
    public void testParseToken() throws Exception {
        //上边生成的token
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJMZWUzNCIsImV4cCI6MTU0NTE0NTc0Mn0.KMvGZVtCtkpkGzVXHeCI03ixgHW09ynyUxMNwQPiW6tahw2HvlV4qBTvpIschm60l06ocJyznRi8zJHkg_PGa9Te8iucZpiGWISOgq7i4F8SqBL68e376as-uH1iyR1gT0AuPxN5CA6gRbPFBvCRk-5p4YiciWBJqka_29J9Bak";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
