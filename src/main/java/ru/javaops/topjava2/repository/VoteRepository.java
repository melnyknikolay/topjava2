package ru.javaops.topjava2.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaops.topjava2.model.Vote;

@Transactional(readOnly = true)
public interface VoteRepository extends BaseRepository<Vote> {
    @Transactional
    @Modifying
    @Query("DELETE FROM Vote v WHERE v.restaurant.id=:id")
    void deleteByRestaurantId(int id);
}