package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.model.game.Game;
import ch.uzh.ifi.hase.soprafs24.model.game.GameParameters;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PostGameStartDTO;
import ch.uzh.ifi.hase.soprafs24.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public PostGameStartDTO createGame(@RequestBody GameParameters gameParameters) {
        Game game = gameService.createGame(gameParameters, 1L); // TODO: remove id and get User from SecContext in Service
        return new PostGameStartDTO(game.getGameId(), game.getGameParameters());
    }
}
