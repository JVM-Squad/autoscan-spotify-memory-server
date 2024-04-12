package ch.uzh.ifi.hase.soprafs24.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "SPOTIFYJWT")
@Getter
@Setter
public class SpotifyJWT {

    @Id
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private Integer expiresln;

    @Column(nullable = false)
    private String tokenType;

    @Column(nullable = false)
    private String scope;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "userId")
    @JsonBackReference
    private User user;
}
