var gameStartCount = 3;
function gameStartCountdown(){
    let gameStartInfo = document.getElementById('gameStartInfo');
    gameStartInfo.style.visibility = "visible";
    setInterval(() => {
        if(gameStartCount >= 0){
            gameStartInfo.innerHTML = "Players ready. Game start in: " + gameStartCount;
        }
         gameStartCount--;

    }, 1000)
}