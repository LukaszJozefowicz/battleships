var socket = null;
var client = null;
var currentUserId;
var activeUsersList = null;
var chatText = "";
document.getElementById("chatInput").value = "";
let authenticatedUserTag = document.getElementById("currentUsername");
let authenticatedUserName = authenticatedUserTag.innerHTML;

window.onload = function load(){
    resetTimer();
    idleLogout();
    connect();
    document.getElementById("chatOutput").value = "";
    document.getElementById("chatInput").focus();
}
window.onbeforeunload = function unload(){
    disconnect();
    window.location = '/logout';
}

document.getElementById("chatInput").addEventListener("keypress", submitMsgOnEnter);

function connect(){

    socket = new SockJS("http://localhost:8080/websocket");

    client = Stomp.over(socket);

    client.connect({}, frame => {

            var payloadBody = null;
            client.subscribe("/gameLobby", payload => {

                payloadBody = JSON.parse(payload.body);
//                console.log("payload body " + payload.body);
//                console.log("json parsed " + payloadBody)
//                console.log("payload body username " + payloadBody.username);
//                console.log("payload body message " + payloadBody.message);
//                console.log("is array " + Array.isArray(payloadBody));
                if(Array.isArray(payloadBody)){
                    let usersTableBody = document.getElementById('users-online-tbody');
                    usersTableBody.remove();
                    activeUsersList = payloadBody;
                    generateUsersTable();
                } else
                if(payloadBody.username !== undefined && payloadBody.message !== undefined){
                    receiveMessage(payloadBody);
                }

            });

            client.send('/ws/listOfUsers', {}, JSON.stringify(activeUsersList));
            client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: " joined the lobby\n"}));

        });

}

function disconnect(){
    if(client != null) {
        let authenticatedUserTag = document.getElementById("currentUsername");
        let authenticatedUserName = authenticatedUserTag.innerHTML;
        activeUsersList = activeUsersList.filter(e => e !== authenticatedUserName);
        if(activeUsersList == 0 || activeUsersList == null)
            activeUsersList = [];

        client.send('/ws/userLeft', {}, JSON.stringify(activeUsersList));
        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: " left the lobby\n"}));

        client.disconnect(payload => {
            client.disconnect();
        });
    }
}


function submitMsgOnEnter(){

    let textInput = document.getElementById("chatInput");
    chatText = textInput.value;


    if(event.which === 13){

        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: chatText}));

        event.target.form.dispatchEvent(new Event("submit", {cancelable: true}));
        event.preventDefault();
        textInput.value = "";
        chatText = "";
    }
}

function receiveMessage(payloadBody){

    let textOutput = document.getElementById("chatOutput");
    textOutput.value += payloadBody.username + ":" + payloadBody.message + "\n";
    textOutput.scrollTop = textOutput.scrollHeight;
}

function generateUsersTable(){
                let usersTable = document.getElementById('users-online')
                let tbody = document.createElement('tbody');
                tbody.id = "users-online-tbody";

                for(var user of activeUsersList){
                    let tr = document.createElement('tr');
                    tr.classList.add('games');
                    tr.classList.add('active-user-row');
                    tr.id = "tr" + user;

                    let td = document.createElement('td');
                    td.classList.add('games');
                    td.innerHTML = user;

                    tr.appendChild(td);
                    tbody.appendChild(tr);
                    usersTable.appendChild(tbody);

                }
}