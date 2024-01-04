package ru.javaops.topjava2.web.vote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaops.topjava2.model.Vote;
import ru.javaops.topjava2.repository.VoteRepository;
import ru.javaops.topjava2.service.VoteService;
import ru.javaops.topjava2.web.AbstractControllerTest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaops.topjava2.web.restaurant.RestaurantTestData.WASABI_ID;
import static ru.javaops.topjava2.web.user.UserTestData.ADMIN_ID;
import static ru.javaops.topjava2.web.user.UserTestData.ADMIN_MAIL;
import static ru.javaops.topjava2.web.user.UserTestData.USER_ID;
import static ru.javaops.topjava2.web.user.UserTestData.USER_MAIL;
import static ru.javaops.topjava2.web.vote.VoteController.REST_URL;
import static ru.javaops.topjava2.web.vote.VoteTestData.*;

class VoteControllerTest extends AbstractControllerTest {
    private static final String REST_URL_SLASH = REST_URL + '/';

    @Autowired
    private VoteRepository repository;

    @Test
    @WithUserDetails(value = USER_MAIL)
    void getOwns() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(VOTE_MATCHER.contentJson(vote_1, vote_2, vote_3));
    }

    @Test
    void getUnAuth() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void getOwnsForToday() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL_SLASH + "by-date"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(VOTE_MATCHER.contentJson(vote_1));
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void getOwnsByDate() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL_SLASH + "by-date")
                .param("date", NOW_MINUS_ONE.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(VOTE_MATCHER.contentJson(vote_4));
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    void voteToday() throws Exception {
        ResultActions action = perform(MockMvcRequestBuilders.post(REST_URL)
                .param("restaurantId", Integer.toString(WASABI_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Vote created = VOTE_MATCHER.readFromJson(action);
        LocalDate date = created.getActualDate();
        VOTE_MATCHER.assertMatch(created, repository.getByUserIdAndDate(ADMIN_ID, date).get());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void revoteTodayBeforeDeadline() throws Exception {
        VoteService.setDeadline(LocalTime.MAX);
        perform(MockMvcRequestBuilders.put(REST_URL)
                .param("restaurantId", Integer.toString(WASABI_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertEquals(repository.getByUserIdAndDate(USER_ID, LocalDate.now()).get().getRestaurantId(), WASABI_ID);
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void revoteTodayAfterDeadline() throws Exception {
        VoteService.setDeadline(LocalTime.MIN);
        perform(MockMvcRequestBuilders.put(REST_URL)
                .param("restaurantId", Integer.toString(WASABI_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void deleteTodayBeforeDeadline() throws Exception {
        VoteService.setDeadline(LocalTime.MAX);
        perform(MockMvcRequestBuilders.delete(REST_URL)
                .param("restaurantId", Integer.toString(WASABI_ID)))
                .andExpect(status().isNoContent());
        assertFalse(repository.getByUserIdAndDate(USER_ID, LocalDate.now()).isPresent());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void deleteTodayAfterDeadline() throws Exception {
        VoteService.setDeadline(LocalTime.MIN);
        perform(MockMvcRequestBuilders.delete(REST_URL)
                .param("restaurantId", Integer.toString(WASABI_ID)))
                .andDo(print())
                .andExpect(status().isConflict());
    }
}