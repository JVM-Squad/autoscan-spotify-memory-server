package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.game.GameState;
import ch.uzh.ifi.hase.soprafs24.constant.user.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.model.game.*;
import ch.uzh.ifi.hase.soprafs24.repository.inMemory.InMemoryGameRepository;
import ch.uzh.ifi.hase.soprafs24.rest.webFilter.UserContextHolder;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class GameService {

    private final UserService userService;
    public InMemoryGameRepository inMemoryGameRepository;

     public Game createGame(GameParameters gameParameters) {
         User host = UserContextHolder.getCurrentUser();
         if (host.getState().equals(UserStatus.INGAME)) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a game");
         }
         Game newGame = new Game(gameParameters, host);
         addPlayerToGame(newGame, host);
         setPlaylistNameAndURL(newGame);
         return inMemoryGameRepository.save(newGame);
     }

    public Game startGame(Integer gameId) {
        Game currentGame = inMemoryGameRepository.findById(gameId).orElseThrow();
        User host = UserContextHolder.getCurrentUser();
        Long hostId = host.getUserId();
        if (currentGame.getPlayers().size() >= GameConstant.getMinPlayers() && Objects.equals(currentGame.getHostId(), hostId)){
            currentGame.setGameState(GameState.ONPLAY);

            randomizePlayersIndex(currentGame);
            createCardCollections(currentGame);
            createScoreBoard(currentGame);
            initiateNewTurn(currentGame);
            return inMemoryGameRepository.save(currentGame);
        } else {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to start the game.");
        }
    }

    private void randomizePlayersIndex(Game currentGame){
        List<User> players = currentGame.getPlayers();
        Collections.shuffle(players); // Set players List to random order.
        currentGame.setPlayers(players);
        inMemoryGameRepository.save(currentGame);
    }

    private void createScoreBoard(Game currentGame){
        List<User> players = currentGame.getPlayers();
        HashMap<Long, Long> scoreBoard = currentGame.getScoreBoard();

        for (User player: players) {
            Long playerId = player.getUserId();
            scoreBoard.put(playerId, 0L);
        }
        currentGame.setScoreBoard(scoreBoard);
        inMemoryGameRepository.save(currentGame);
    }

    private void initiateNewTurn(Game currentGame){
        List<User> players = currentGame.getPlayers();
        Long activePlayer = currentGame.getActivePlayer();
        int activePlayerIndex;

        if (activePlayer == null) {
            activePlayerIndex = 0;
        } else {
            activePlayerIndex = players.indexOf(userService.findUserByUserId(activePlayer));
            activePlayerIndex++;
            if (activePlayerIndex == players.size()){
                activePlayerIndex = 0;
            }
        }

        currentGame.setActivePlayer(players.get(activePlayerIndex).getUserId());
        Turn turn = new Turn(activePlayer);
        currentGame.getHistory().add(turn);
        inMemoryGameRepository.save(currentGame);
    }

     public List<Game> getGames() {
         return inMemoryGameRepository.findAll();
     }

     public List<User> addPlayerToGame(Integer gameId) {
         User newUser = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId).orElseThrow();
         return addPlayerToGame(game, newUser);
     }

     private void createCardCollections(Game currentGame){
        User host = UserContextHolder.getCurrentUser();
        CardCollection cardCollection = new CardCollection(currentGame.getGameParameters(), host.getSpotifyJWT().getAccessToken());
        currentGame.setCardCollection(cardCollection);
        inMemoryGameRepository.save(currentGame);
     }

     public List<User> removePlayerFromGame(Integer gameId) {
         User userToRemove = UserContextHolder.getCurrentUser();
         Game game = inMemoryGameRepository.findById(gameId).orElseThrow();
         userService.setPlayerState(userToRemove, UserStatus.ONLINE);
         if (game.getHostId().equals(userToRemove.getUserId())) {
             inMemoryGameRepository.deleteById(gameId);
             return null;
         } else {
             game.getPlayers().removeIf(u -> u.getUserId().equals(userToRemove.getUserId()));
             return inMemoryGameRepository.save(game).getPlayers();
         }
     }

    private List<User> addPlayerToGame(Game game, User user) {
         userService.setPlayerState(user, UserStatus.INGAME);
         if (game.getGameState() == GameState.OPEN && !game.getPlayers().contains(user)){
             game.getPlayers().add(user);
             return inMemoryGameRepository.save(game).getPlayers();
         } else {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
         }
    }

    private Game setPlaylistNameAndURL(Game game) {
         HashMap<String,String> playlistMetadata = SpotifyService.getPlaylistMetadata(
                 UserContextHolder.getCurrentUser().getSpotifyJWT().getAccessToken(),
                 game.getGameParameters().getPlaylist().getPlaylistId()
         );

        game.getGameParameters().getPlaylist().setPlaylistName(playlistMetadata.get("playlist_name"));
        game.getGameParameters().getPlaylist().setPlaylistImageUrl(playlistMetadata.get("image_url"));

        return inMemoryGameRepository.save(game);
    }

    public void runTurn(Integer gameId) {

    }
}
