package com.autotest.util.sm;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Date;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.util.encoders.Hex;

public class SMUtils {

	/**
	 * 获取sm2的秘钥对
	 * @return  公钥 私钥 json串
	 */
	public static SM2Key generateSm2Key() {
		Map<String,String> keyMap = SM2Utils.generateKeyPair();
        String publicKey = keyMap.get("publicKey");  //加密密钥
        String privateKey = keyMap.get("privateKey"); //解密蜜月
        SM2Key key = new SM2Key();
        key.setPrivateKey(privateKey);
        key.setPublicKey(publicKey);
        key.setCreateDate(new Date());
        return key;
	}
	
	/**
	 * SM2 加密
	 * @param publicKey 公钥
	 * @param input  原始数据
	 * @return
	 * @throws Exception
	 */
	public static String sm2Encrypt(String publicKey, String input) throws Exception {
		String encString = SM2Utils.encrypt(SecurityUtils.hexStringToBytes(publicKey),input.getBytes());
		return encString;
	}
	
	
	/**
	 * SM2 解密  
	 * @param privateKey  私钥
	 * @param encryptedData  加密后的数据
	 * @return
	 * @throws Exception
	 */
	public static String sm2decrypt(String privateKey, String encryptedData) throws Exception {
		byte[] plainString = SM2Utils.decrypt(SecurityUtils.hexStringToBytes(privateKey),SecurityUtils.hexStringToBytes(encryptedData));
        return new String(plainString);
	}
	
	/**
	 * SM3 加密
	 * @param input
	 * @return
	 */
	public static String sm3Encrypt(String input){
		byte[] md = new byte[32];
		byte[] msg1 = input.getBytes();
		SM3Utils sm3 = new SM3Utils();
		sm3.update(msg1, 0, msg1.length);
		sm3.doFinal(md, 0);
		String s = new String(Hex.encode(md)).toUpperCase();
		return s;
	}
	
	/**
	 * SM3 加密
	 * @param input
	 * @return
	 */
	public static boolean sm3Check(String input,String encryptedData){
		byte[] md = new byte[32];
		byte[] msg1 = input.getBytes();
		SM3Utils sm3 = new SM3Utils();
		sm3.update(msg1, 0, msg1.length);
		sm3.doFinal(md, 0);
		String s = new String(Hex.encode(md)).toUpperCase();
		return s.equals(encryptedData);
	}
	
	/**
	 * 获取sm4的秘钥
	 * @return sm4秘钥
	 * @throws Exception
	 */
	public static String generateSm4Key() throws Exception {
		byte[] sm4key = SM4Utils.generateKey();
		String key = Util4Hex.bytesToHexString(sm4key);
		return key;
	}
	
	/**
	 * sm4 Ecb加密
	 * @param key
	 * @param input
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static String sm4_ecb_Encrypt(String key,String input) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		 byte[] cipherText = SM4Utils.encrypt_Ecb_Padding(Util4Hex.hexStringToBytes(key), input.getBytes());
		 String cipherTextString = Util4Hex.bytesToHexString(cipherText);
		 return cipherTextString;
	}
	
	/**
	 * 
	 * @param key
	 * @param cipherText
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 */
	public static String sm4_ecb_decrypt(String key,String cipherText) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException  {
		byte[] decryptedData = SM4Utils.decrypt_Ecb_Padding(Util4Hex.hexStringToBytes(key), Util4Hex.hexStringToBytes(cipherText));
		return new String(decryptedData);
	}
	
	
	/**
	 * SM4 CBC 加密 
	 * @param key  秘钥
	 * @param iv   iv偏移量密钥
	 * @param input  原始数据
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public static String sm4_cbc_Encrypt(String key,String iv,String input) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
		 byte[] cipherText = SM4Utils.encrypt_Cbc_Padding(Util4Hex.hexStringToBytes(key), Util4Hex.hexStringToBytes(iv),input.getBytes());
		 String cipherTextString = Util4Hex.bytesToHexString(cipherText);
		 return cipherTextString;
	}
	
	/**
	 * SM4 Cbc解密
	 * @param key 秘钥
	 * @param iv iv偏移量密钥
	 * @param cipherText   加密后的数据
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	public static String sm4_cbc_decrypt(String key,String iv,String cipherText) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException  {
		byte[] decryptedData = SM4Utils.decrypt_Cbc_Padding(Util4Hex.hexStringToBytes(key),Util4Hex.hexStringToBytes(iv), Util4Hex.hexStringToBytes(cipherText));
		return new String(decryptedData);
	}
	
	public static void main(String[] args) throws Exception {
		generateSm4Key();
	}
}
