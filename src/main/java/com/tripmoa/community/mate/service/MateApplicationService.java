package com.tripmoa.community.mate.service;

import com.tripmoa.community.mate.domain.MateApplication;
import com.tripmoa.community.mate.domain.MateDomain;
import com.tripmoa.community.mate.domain.MatePost;
import com.tripmoa.community.mate.dto.ApplicationRequest;
import com.tripmoa.community.mate.dto.ApplicationResponse;
import com.tripmoa.community.mate.dto.MateResponse;
import com.tripmoa.community.mate.enums.ApplyStatus;
import com.tripmoa.community.mate.repository.ApplicationRepository;
import com.tripmoa.community.mate.repository.MateRepository;
import com.tripmoa.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MateApplicationService {

    private final MateDomain domain;
    private final MateRepository mateRepository;
    private final ApplicationRepository applyRepository;

    public List<ApplicationResponse> getReceivedApplication(Long ownerId) {
        List<MateApplication> applications  = this.applyRepository.findByOwnerId(ownerId); // 내가 쓴 포스트로 조회
        return applications.stream()
                .map(ApplicationResponse::from)
                .toList();
    }

    public List<ApplicationResponse> getSentApplication(Long applicantId) {
        List<MateApplication> applications  = this.applyRepository.findByApplicantId(applicantId); // 내가 쓴 신청서로 조회
        return applications.stream()
                .map(ApplicationResponse::from)
                .toList();
    }


    public ApplicationResponse createApply(Long postId, ApplicationRequest request, User applicant) {
        MatePost post = this.mateRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post NOT Found"));

        MateApplication application = MateApplication.builder()
                .matePost(post)
                .applicant(applicant)
                .content(request.getContent())
                .status(ApplyStatus.PENDING)
                .build();

        applyRepository.save(application);

        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse approveApply(Long applyPostId, User author) {
        MateApplication application = this.applyRepository.findById(applyPostId)
                .orElseThrow(() -> new RuntimeException("Application NOT Found"));

        if(!application.isAuthor(author)) {
            throw new IllegalStateException("작성자만 승인할 수 있습니다.");
        }

        application.approve();
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse rejectApply(Long applyPostId, User author) {
        MateApplication application = this.applyRepository.findById(applyPostId)
                .orElseThrow(() -> new RuntimeException("Application NOT Found"));

                if(!application.isAuthor(author)) {
                    throw new IllegalStateException("작성자만 승인할 수 있습니다.");
                }

        application.reject();
        return ApplicationResponse.from(application);


    }

}
