package com.autotest.util.sm;

import java.io.Serializable;
import java.util.Date;

//import lombok.Data;

//@Data
public class SM2Key  implements Serializable{
	private static final long serialVersionUID = 1L;
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	private String publicKey;
	private String privateKey;
	private Date createDate;
}
