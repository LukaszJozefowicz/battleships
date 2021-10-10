var gameStartCount = 3;
function gameStartCountdown(){

    document.getElementById('gameStartInfo')

    let gameStartInfo = document.getElementById('gameStartInfo');
    gameStartInfo.style.visibility = "visible";
    setInterval(() => {
        if(gameStartCount >= 0){
            gameStartInfo.innerHTML = "Game start in: " + gameStartCount;
        }
         gameStartCount--;

    }, 1000)
}