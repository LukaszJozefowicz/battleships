var t;
var currentCount = 40;
    function idleLogout() {
        //window.onload = resetTimer;
        //window.onmousemove = resetTimer;
        //window.onmousedown = resetTimer; // catches touchscreen presses
        window.onclick = resetTimer;     // catches touchpad clicks
        //window.onscroll = resetTimer;    // catches scrolling with arrow keys
        //window.onkeypress = resetTimer;

}

function logout() {
        console.log("logout function");
        window.location = '/logout';
}

function resetTimer() {
        console.log("reset timer function");
        currentCount = 40;
        clearTimeout(t);
        t = setTimeout(logout, 40000);  // time is in milliseconds
}

setInterval(function(){
     let minutes = Math.floor(currentCount / 60);
     let seconds = Math.floor(currentCount % 60);
     document.getElementById("countdown").innerHTML = " " + minutes + ":" + (seconds<10 ? "0" : "") + seconds;
     currentCount--;

}, 1000)