var t;
var currentCount = 600;
    function idleLogout() {
        window.onclick = resetTimer;
}

function logout() {
        console.log("logout function");
        window.location = '/logout';
}

function resetTimer() {
        console.log("reset timer function");
        currentCount = 600;
        clearTimeout(t);
        t = setTimeout(logout, 600000);
        setTimeout(logout, 600000);
}

setInterval(function(){
     let minutes = Math.floor(currentCount / 60);
     let seconds = Math.floor(currentCount % 60);
     document.getElementById("countdown").innerHTML = " " + minutes + ":" + (seconds<10 ? "0" : "") + seconds;
     currentCount--;

}, 1000)