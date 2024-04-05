package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class GameService {

    public InMemoryGameRepository inMemoryGameRepository;

    public UserRepository userRepository;

     public Game createGame(GameParameters gameParameters, Long hostId) {
         User host = userRepository.findByUserId(hostId);
         Game newGame = new Game(gameParameters, host);
         return inMemoryGameRepository.save(newGame);
     }
}
