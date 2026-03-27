package com.tripmoa.security.oauth;

import com.tripmoa.user.entity.SocialAccount;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.Gender;
import com.tripmoa.user.enums.ProfileType;
import com.tripmoa.user.enums.Provider;
import com.tripmoa.user.enums.UserStatus;
import com.tripmoa.user.repository.SocialAccountRepository;
import com.tripmoa.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 소셜 로그인 성공 시
 * OAuth2 서버(구글/카카오)에서 받은 사용자 정보를 우리 서비스 User 엔티티로 변환하는 클래스
 */

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    // 소셜 로그인 성공 시 자동 호출되는 메서드
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {

        // 기본 OAuth2 서비스로부터 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 어떤 소셜 로그인인지 확인 (google / kakao)
        String registrationId =
                userRequest.getClientRegistration()
                        .getRegistrationId();

        // OAuth2에서 받은 사용자 정보(Map 형태)
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 소셜 로그인별 사용자 정보 파싱
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory
                .getOAuth2UserInfo(registrationId, attributes);

        // DB에 이미 있는 사용자인지 확인
        User user = userRepository
                .findByEmail(userInfo.getEmail())
                .orElseGet(() -> createUser(userInfo, registrationId));

        // CustomOAuth2User로 감싸서 반환
        return new CustomOAuth2User(user, attributes);
    }

    // 최초 로그인 사용자 생성
    private User createUser(OAuth2UserInfo userInfo, String registrationId) {

        User user = new User();
        user.setEmail(userInfo.getEmail());

        // 닉네임: 이름이 있으면 사용, 없으면 이메일 앞자리 사용 (에러 방지)
        String nickname = userInfo.getName();
        if (nickname == null || nickname.isBlank()) {
            nickname = userInfo.getEmail().split("@")[0];
        }
        user.setNickname(nickname);

        // 이미지: 소셜 사진이 없으면 랜덤 아바타 할당
        if (userInfo.getImageUrl() == null || userInfo.getImageUrl().isBlank()) {
            user.setProfileType(ProfileType.AVATAR);
            user.setAvatarEmoji("😊");
            user.setAvatarColor("#FFE5E5");
        } else {
            user.setProfileType(ProfileType.CUSTOM);
            user.setProfileImage(userInfo.getImageUrl());
        }

        user.setStatus(UserStatus.ACTIVE);

        // 이름 정보가 있으면 저장하고 잠금
        if (userInfo.getName() != null && !userInfo.getName().isBlank()) {
            user.setName(userInfo.getName());
            user.setNameLocked(true);
        }

        // 성별 정보가 있으면 저장하고 잠금 (Enum 변환 예외 처리 추가)
        try {
            String genderStr = userInfo.getGender();
            if (genderStr != null) {
                user.setGender(Gender.valueOf(genderStr.toUpperCase()));
                user.setGenderLocked(true);
            }
        } catch (Exception e) {
            // 형식 안 맞으면 잠그지 않고 넘어감
        }

        // 생년월일 정보가 있으면 저장하고 잠금
        String birth = userInfo.getBirthDay();
        if (birth != null && !birth.isBlank()) {
            user.setBirthDate(java.time.LocalDate.parse(birth));
            user.setBirthLocked(true);
        }

        // DB 저장
        User savedUser = userRepository.save(user);

        // 소셜 계정 연결 정보 저장
        SocialAccount social = new SocialAccount();
        social.setUser(savedUser);
        social.setProvider(Provider.valueOf(registrationId.toUpperCase()));
        social.setProviderUserId(userInfo.getId());
        social.setConnected(true);
        social.setCreatedAt(LocalDateTime.now());

        socialAccountRepository.save(social);

        return savedUser;
    }

}

