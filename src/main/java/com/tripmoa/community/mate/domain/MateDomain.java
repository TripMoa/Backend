package com.tripmoa.community.mate.domain;

import com.tripmoa.community.mate.Exception.InvalidBudgetException;
import com.tripmoa.community.mate.Exception.InvalidParticipantInfoException;
import com.tripmoa.community.mate.Exception.InvalidScheduleException;
import com.tripmoa.community.mate.Exception.ParticipantFullException;
import com.tripmoa.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MateDomain {

    public void validateCreate(MatePost post) {
        validateSchedule(post.getStartDate(), post.getEndDate());
        validateParticipantInfo(post.getCurrentParticipant(), post.getMaxParticipant());
        validateBudget(post.getBudget());
    }

    private void validateSchedule(LocalDate startDate, LocalDate endDate) {
        if(endDate.isBefore(startDate)) {
            throw InvalidScheduleException.endDateBeforeStart();
        }
        if(startDate.isBefore(LocalDate.now())) {
            throw InvalidScheduleException.startDateBeforeNow();
        }
    }

    private void validateParticipantInfo(Integer current, Integer max) {
        if(current < 1) {
            throw InvalidParticipantInfoException.currentTooLow();
        }
        if(max < 2) {
            throw InvalidParticipantInfoException.maxTooLow();
        }
        if(current > max) {
            throw InvalidParticipantInfoException.currentExceedsMax();
        }
        if(current.equals(max)) {
            throw InvalidParticipantInfoException.alreadyFullyBooked();
        }
    }

    private void validateBudget(Integer budget) {
        if(budget < 0) {
            throw new InvalidBudgetException();
        }
    }

    public boolean isFullyBooked(MatePost post) {
        return post.getCurrentParticipant()>=post.getMaxParticipant();
    }

    public void validateApply(MatePost post) {
        if(isFullyBooked(post)) {
            throw new ParticipantFullException();
        }
    }

    public boolean isAuthor(MatePost post, User user) {
        return post.getUser().getId().equals(user.getId());
    }
}
