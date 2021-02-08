package com.hissummer.mockserver.mgmt.service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import com.hissummer.mockserver.mgmt.entity.User;
import com.hissummer.mockserver.mgmt.service.jpa.UserMongoRepository;

@Service
public class UserServiceImpl {
	@Autowired
	UserMongoRepository userMongoRepository;

	public boolean login(String username, String password) {

		try {

			if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {

				User loginuser = userMongoRepository.findByUsernameAndPassword(username, md5password(password));

				if (loginuser == null && username.equals("admin") && password.equals("hissummer.com")) {
					// 第一次创建用户
					this.createUser(username, password);
					return activeUser(loginuser);
				}
				if (loginuser != null) {
					return activeUser(loginuser);

				}

			}

		} catch (Exception e) {
			return false;
		}
		return false;

	}

	public User finduserByuserName(String username) {

		return userMongoRepository.findByUsername(username);

	}

	private String md5password(String password) {

		return DigestUtils.md5DigestAsHex(password.getBytes());

	}

	public boolean logout(String username, Cookie[] cookies) {

		if (!StringUtils.isEmpty(username)) {
			User loginuser = userMongoRepository.findByUsername(username);
			if (loginuser == null || !loginuser.getId().equals(this.getCookieMu(cookies))) {

				return false;
			}

			loginuser.setLoginExpiredDate(null);
			userMongoRepository.save(loginuser);
		}

		return true;

	}

	// not used now!
	public boolean logout(User user, Cookie[] cookies) {

		if (user == null)
			return false;

		user.setLoginExpiredDate(null);

		userMongoRepository.save(user);

		return true;

	}

	public boolean createUser(String username, String password) {

		if (userMongoRepository.findByUsername(username) != null) {
			return false;
		}
		User user = User.builder().username(username).password(md5password(password)).createDate(new Date())
				.enable(true).build();
		userMongoRepository.insert(user);

		return true;
	}

	public boolean rePasswordUser(String username, String password, String newpassword, Cookie[] cookies) {

		if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {

			User loginuser = userMongoRepository.findByUsernameAndPassword(username, md5password(password));

			if (loginuser != null && loginuser.getId().equals(this.getCookieMu(cookies))) {

				loginuser.setPassword(this.md5password(newpassword));
				userMongoRepository.save(loginuser);
				return true;
			} else {

				return false;

			}

		}

		return false;

	}

	public boolean delUser(String username, String password, Cookie[] cookies) {

		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
			return false;

		User user = userMongoRepository.findByUsernameAndPassword(username, md5password(password));

		if (user == null || !user.getId().equals(this.getCookieMu(cookies))) {
			return false;
		}
		userMongoRepository.delete(user);

		return true;

	}

	public boolean isUserLoginWithMuId(String muId) {

		Optional<User> loginuser = userMongoRepository.findById(muId);

		if (loginuser == null || !loginuser.isPresent()) {

			return false;
		}

		if (loginuser.get().getLoginExpiredDate() == null || (loginuser.get().getLoginExpiredDate() != null
				&& loginuser.get().getLoginExpiredDate().before(new Date()))) {

			return false;
		}

		this.activeUser(loginuser.get());

		return true;
	}

	public boolean isUserLogin(String username) {

		User loginuser = userMongoRepository.findByUsername(username);

		if (loginuser == null) {

			return false;
		}

		if (loginuser.getLoginExpiredDate().before(new Date())) {

			return false;
		}

		this.activeUser(loginuser);

		return true;

	}

	public boolean activeUser(String username) {

		try {

			if (!StringUtils.isEmpty(username)) {
				User loginuser = userMongoRepository.findByUsername(username);
				return activeUser(loginuser);
			}
		} catch (Exception e) {
		}
		return false;

	}

	private boolean activeUser(User loginuser) {
		if (loginuser == null) {
			return false;
		}
		Calendar nowdate = Calendar.getInstance();
		nowdate.add(Calendar.DAY_OF_YEAR, 2);
		loginuser.setLoginExpiredDate(nowdate.getTime());

		userMongoRepository.save(loginuser);

		return true;
	}

	private String getCookieMu(Cookie[] cookies) {

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("mu"))
				return cookie.getValue();
		}

		return "muisnotdefined";
	}

}
