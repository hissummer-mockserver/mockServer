package com.hissummer.mockserver.mgmt.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.hissummer.mockserver.mgmt.service.jpa.UserMongoRepository;
import com.hissummer.mockserver.mgmt.vo.Loginpair;

@Service
public class UserService {
	@Autowired
	UserMongoRepository userMongoRepository;

	public boolean login(String username, String password) {

		try {
			
		if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
						
			
			Loginpair loginuser = userMongoRepository.findByUsernameAndPassword(username, md5password(password));
			
			if(loginuser==null && username.equals("admin") && password.equals("hissummer.com"))
			{
				//第一次创建用户
			    this.createUser(username, password);
			    return activeUser(loginuser);
			}
			if(loginuser !=null)
			{
			    return activeUser(loginuser);

			}

			
		}

		}
		catch(Exception e) {
			return false;
		}
		this.logout(username);
		return false;

	}
	
	public Loginpair finduserByuserName(String username) {
		
		return userMongoRepository.findByUsername(username);

	}
	
	private String md5password(String password) {
		
		return DigestUtils.md5DigestAsHex(password.getBytes());
		
	}
	
	public boolean logout(String username) {

		if (!StringUtils.isEmpty(username) ) {
			Loginpair loginuser = userMongoRepository.findByUsername(username);
			if (loginuser == null) {

				return false;
			}
			
			loginuser.setLoginExpiredDate(null);

			userMongoRepository.save(loginuser);
		}

		return true;

	}
	
	public boolean logout(Loginpair user) {

		if(user == null) return false;
			user.setLoginExpiredDate(null);

			userMongoRepository.save(user);

		return true;

	} 
	
	
	public boolean createUser (String username , String password) {
		
		if(userMongoRepository.findByUsername(username) != null)
		{
			return false;
		}
		Loginpair user = Loginpair.builder().username(username).password(md5password(password)).createDate(new Date()).enable(true).build();
		userMongoRepository.insert(user);
		
		return true;
	}
	
	
	public boolean delUser(String username) {
		
		Loginpair user = userMongoRepository.findByUsername(username);
		if(user==null) {
			return false;
		}
		userMongoRepository.delete(user);
		
		return true;
		
	}
	
	public boolean isUserLoginWithMuId(String muId) {
				
		Optional<Loginpair> loginuser = userMongoRepository.findById(muId);
		
		if (loginuser == null || !loginuser.isPresent()) {

			return false;
		}
		
		if( loginuser.get().getLoginExpiredDate() == null ||  ( loginuser.get().getLoginExpiredDate() != null && loginuser.get().getLoginExpiredDate().before(new Date())) ) {
			
			return false;
		}
		
		this.activeUser(loginuser.get());
	
	    return true;
	}
	
	
	public boolean isUserLogin(String username) {
		
		
		Loginpair loginuser = userMongoRepository.findByUsername(username);

		if (loginuser == null) {

			return false;
		}
		
		if( loginuser.getLoginExpiredDate().before(new Date())) {
			
			return false;
		}
	
		this.activeUser(loginuser);

	    return true;
	
	}
	
	
	public boolean activeUser(String username) {
				
		try {
			
		if (!StringUtils.isEmpty(username) ) {
			Loginpair loginuser = userMongoRepository.findByUsername(username);
			return activeUser(loginuser);
		}		
		}
		catch(Exception e) {
		}
		return false;

	}
	
	private boolean activeUser(Loginpair loginuser)
	{
		if (loginuser == null) {
			return false;
		}
		Calendar nowdate = Calendar.getInstance();
		nowdate.add(Calendar.DAY_OF_YEAR, 2);
		loginuser.setLoginExpiredDate(nowdate.getTime());

		userMongoRepository.save(loginuser);
		
		return true;
	}
	
	

}
