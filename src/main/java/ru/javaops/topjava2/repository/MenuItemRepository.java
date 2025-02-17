package ru.javaops.topjava2.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaops.topjava2.error.DataConflictException;
import ru.javaops.topjava2.model.MenuItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface MenuItemRepository extends BaseRepository<MenuItem> {

    @Query("SELECT mi FROM MenuItem mi WHERE mi.id=:id AND mi.restaurant.id=:restaurantId")
    Optional<MenuItem> get(int restaurantId, int id);

    @Query("SELECT mi from MenuItem mi WHERE mi.restaurant.id=:restaurantId AND mi.actualDate = :date ORDER BY mi.name ASC")
    List<MenuItem> getByRestaurantAndDate(int restaurantId, LocalDate date);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.restaurant.id=:restaurantId ORDER BY mi.actualDate DESC, mi.name ASC")
    List<MenuItem> getByRestaurant(int restaurantId);

    default MenuItem checkExistOrBelong(int restaurantId, int id) {
        return get(restaurantId, id).orElseThrow(
                () -> new DataConflictException("MenuItem id=" + id + " doesn't exist or belong to Restaurant id=" + restaurantId));
    }
}