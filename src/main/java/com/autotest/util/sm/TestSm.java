package com.autotest.util.sm;

public class TestSm {
	public static void main(String[] args) throws Exception {
		//sm2 秘钥
		SM2Key key = SMUtils.generateSm2Key();
		String privateKey = key.getPrivateKey();
		String publicKey = key.getPublicKey();
		System.out.println("公钥："+publicKey);
		System.out.println("私钥："+privateKey);
		
		//sm2加密
		String input = "恒泰实达";
		String sm2EncryptData = SMUtils.sm2Encrypt(publicKey, input);
		System.out.println("密文："+sm2EncryptData);
		
		//sm2解密
		String result = SMUtils.sm2decrypt(privateKey, sm2EncryptData);
		System.out.println("sm2解密后字符串："+result);
		
		//sm3加密
		String sm3Input = "sm3加密";
		String sm3EncryptData = SMUtils.sm3Encrypt(sm3Input);
		System.out.println("sm3加密后数据："+sm3EncryptData);
		
		//sm3 check
		boolean sm3Check  = SMUtils.sm3Check(sm3Input, sm3EncryptData);
		System.out.println("sm3检查结果："+sm3Check);
		
		//sm4 获取秘钥
		String sm4Key = SMUtils.generateSm4Key();
		String iv = SMUtils.generateSm4Key();
		System.out.println("sm4 秘钥："+sm4Key);
		System.out.println("sm4 iv："+iv);
		
		//sm4 ECB 方式加密解密
		String DATA = "恒泰实达111";
	    String cipherTextString = SMUtils.sm4_ecb_Encrypt(sm4Key,DATA);
	    System.out.println("ECB加密后的数据："+cipherTextString);
	    String text = SMUtils.sm4_ecb_decrypt(sm4Key,cipherTextString);
	    System.out.println("ECB解密后的数据："+text);
	    
	    //sm4 Cbc 方式加密解密
	    String DATA1 = "恒泰实达222";
	    String cipherTS = SMUtils.sm4_cbc_Encrypt(sm4Key,iv,DATA1);
	    System.out.println("CBC加密后的数据："+cipherTS);
	    String t = SMUtils.sm4_cbc_decrypt(sm4Key,iv,cipherTS);
	    System.out.println("CBC解密后的数据："+t);
		
	}
}
