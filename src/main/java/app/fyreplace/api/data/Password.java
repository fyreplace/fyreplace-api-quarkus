package app.fyreplace.api.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "passwords")
public class Password extends EntityBase {
    @OneToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User user;

    @Column(nullable = false)
    public String password;
}
