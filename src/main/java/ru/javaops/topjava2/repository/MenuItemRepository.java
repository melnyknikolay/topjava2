package ru.javaops.topjava2.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaops.topjava2.model.MenuItem;

@Transactional(readOnly = true)
public interface MenuItemRepository extends BaseRepository<MenuItem> {
}