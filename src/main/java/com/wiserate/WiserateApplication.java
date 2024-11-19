package com.wiserate;

import com.wiserate.models.MUser;
import com.wiserate.enums.MUserRoles;
import com.wiserate.services.MUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WiserateApplication implements CommandLineRunner {

	private final MUserService mUserService;

	@Autowired
    public WiserateApplication(MUserService mUserService) {
        this.mUserService = mUserService;
    }

    public static void main(String[] args) {
		SpringApplication.run(WiserateApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// CREATE TEST USER
		MUser user = new MUser("jackson", "jackson", "jackson@gmail.com");
		MUser admin = new MUser("jack", "jackson", "jackson@gmail.com", MUserRoles.ADMIN);
		mUserService.createMUser(user);
		mUserService.createMUser(admin);
	}

}
