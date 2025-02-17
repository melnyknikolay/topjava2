package ru.javaops.topjava2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.lang.Nullable;
import ru.javaops.topjava2.View;
import ru.javaops.topjava2.util.validation.NoHtml;

import java.util.List;

@Entity
@Table(name = "restaurant", uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "address"}, name = "uk_restaurant")})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(callSuper = true)
public class Restaurant extends NamedEntity {

    @Column(name = "enabled", nullable = false, columnDefinition = "bool default true")
    private boolean enabled = true;

    @Column(name = "address")
    @Size(max = 1024)
    @NoHtml
    @Nullable
    private String address;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "restaurant")
    @OrderBy("name ASC")
    @OnDelete(action = OnDeleteAction.CASCADE) //https://stackoverflow.com/a/44988100/548473
    @JsonView(View.RestaurantWithMeals.class)
    @Schema(hidden = true)
    @ToString.Exclude
    @JsonInclude(JsonInclude.Include.NON_EMPTY) //https://stackoverflow.com/a/27964775/548473
    private List<MenuItem> menuItems;

    public Restaurant(Integer id, String name, @Nullable String address) {
        super(id, name);
        this.address = address;
    }
}