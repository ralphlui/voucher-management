package sg.edu.nus.iss.springboot.voucher.management.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;

import sg.edu.nus.iss.springboot.voucher.management.configuration.VourcherManagementSecurityConfig;
import sg.edu.nus.iss.springboot.voucher.management.dto.UserDTO;
import sg.edu.nus.iss.springboot.voucher.management.entity.User;
import sg.edu.nus.iss.springboot.voucher.management.repository.*;
import sg.edu.nus.iss.springboot.voucher.management.service.IUserService;
import sg.edu.nus.iss.springboot.voucher.management.utility.AmazonSES;
import sg.edu.nus.iss.springboot.voucher.management.utility.DTOMapper;
import sg.edu.nus.iss.springboot.voucher.management.utility.EncryptionUtils;

@Service
public class UserService implements IUserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	
	
	@Autowired
	private  UserRepository userRepository;
	
	
	@Autowired
	private EncryptionUtils encryptionUtils;
	
	@Autowired
	private VourcherManagementSecurityConfig securityConfig;


	@Override
	public List<User> findByIsActiveTrue() {
		return userRepository.findByIsActiveTrue();
	}

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public User findByEmailAndStatus(String email, boolean isActive) {

		return userRepository.findByEmailAndStatus(email, isActive);
	}

	@Override
	public User create( User user) {
		try {
			String encodedPassword = passwordEncoder.encode(user.getPassword());
			user.setPassword(encodedPassword);
			String code = UUID.randomUUID().toString();
			user.setVerificationCode(code);
			user.setVerified(false);
			user.setActive(true);
			user.setCreatedDate(LocalDateTime.now());

			User createdUser = userRepository.save(user);

			if (createdUser != null) {
				sendVerificationEmail(createdUser);
			}

			
			return createdUser;

		} catch (Exception e) {
			logger.error("Error occurred while user creating, " + e.toString());
			e.printStackTrace();

		}

		return new User();
	}

	@Override
	public User update(User user) {
		try {

			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setUpdatedDate(LocalDateTime.now());
			return userRepository.save(user);

		} catch (Exception e) {
			logger.error("Error occurred while user updating, " + e.toString());
			e.printStackTrace();

		}

		return new User();
	}

	@Override
	public User findByEmail(String email) {

		return userRepository.findByEmail(email);
	}

	@Override
	public User validateUserLogin(String email, String password) {
		try {
			User user = userRepository.findByEmail(email);
			if (user != null && passwordEncoder.matches(password, user.getPassword())) {
				return user;
			}
		} catch (Exception e) {
			logger.error("Error occurred while validateUserLogin, " + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Optional<User> findById(String userId) {
		// TODO Auto-generated method stub
		return userRepository.findById(userId);
	}

	private void sendVerificationEmail(User user) {

		try {
			
			AmazonSimpleEmailService client =securityConfig.sesClient();
			String from=securityConfig.getEmailFrom().trim();
			String clientURL = securityConfig.getFrontEndUrl().trim();
			

			String to = user.getEmail();

			String verificationCode = encryptionUtils.encrypt(user.getVerificationCode());
			
			String verifyURL = clientURL + "/components/register/verify/" + verificationCode.trim();
            logger.info("verifyURL... {}",verifyURL);

			String subject = "Please verify your registration";
			String body = "Dear [[name]],<br><br>" + "Thank you for choosing our service.<br>"
					+ "To complete your registration, please click the link below to verify :<br>"
					+ "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>" + "Thank you" + "<br><br>"
					+ "<i>(This is an auto-generated email, please do not reply)</i>";

			body = body.replace("[[name]]", user.getUsername());
			
			body = body.replace("[[URL]]", verifyURL);

			AmazonSES.sendEmail(client, from, Arrays.asList(to), subject, body);
		} catch (Exception e) {
			logger.error("Error occurred while sendVerificationEmail, " + e.toString());
			e.printStackTrace();
		}
	}
	
	@Override
	public UserDTO verify(String verificationCode) {

		try {
			String decodedVerificationCode = encryptionUtils.decrypt(verificationCode);

			User user = userRepository.findByVerificationCode(decodedVerificationCode, false, true);

			if (user != null) {
				user.setVerified(true);
				user.setUpdatedDate(LocalDateTime.now());
				User updatedUser = userRepository.save(user);
				if (updatedUser != null) {
					return DTOMapper.toUserDTO(updatedUser);
				}

			}
		} catch (Exception e) {
			logger.error("Error occurred while user verify, " + e.toString());
			e.printStackTrace();
		}
		return null;
	}
}
