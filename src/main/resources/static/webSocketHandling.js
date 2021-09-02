var socket = null;
var client = null;
var currentUserId;
var activeUsersList = null;
var chatText = "";
document.getElementById("chatInput").value = "";
let authenticatedUserTag = document.getElementById("currentUsername");
let authenticatedUserName = authenticatedUserTag.innerHTML;

window.onload = function load(){
    console.log("page loaded");
    connect();
    document.getElementById("chatOutput").value = "";
    document.getElementById("chatInput").focus();
}
window.onbeforeunload = function unload(){
    console.log("page left");
    disconnect();
}

document.getElementById("chatInput").addEventListener("keypress", submitMsgOnEnter);

function connect(){
    // Try to set up WebSocket connection with the handshake at "http://localhost:8080/stomp"
    socket = new SockJS("http://localhost:8080/websocket");

    // Create a new StompClient object with the WebSocket endpoint
    client = Stomp.over(socket);

    // Start the STOMP communications, provide a callback for when the CONNECT frame arrives.
    /*client.connect({}, frame => {

        client.subscribe("/gameLobby", payload => {

            let usersList = document.getElementById('usersList');
            //let joinedUser = document.createElement('li');
            //joinedUser.id = JSON.parse(payload.body);
            activeUsersList = JSON.parse(payload.body);
            console.log("payload body " + payload.body);
            console.log("json parsed " + JSON.parse(payload.body))
            //currentUserId = joinedUser.id;
            for(var user of activeUsersList){
                let joinedUser = document.createElement('li');
                joinedUser.appendChild(document.createTextNode(user));
                usersList.appendChild(joinedUser);
            }

//            joinedUser.appendChild(document.createTextNode(JSON.parse(payload.body)));
//            usersList.appendChild(joinedUser);

            console.log("Connected!!!!!!");
        });

        //let authenticatedUserTag = document.getElementById("currentUsername");
        //let authenticatedUserName = authenticatedUserTag.innerHTML;
        client.send('/ws/listOfUsers', {}, JSON.stringify(activeUsersList));
        console.log("stringified data: " + JSON.stringify(activeUsersList));

    });*/
    client.connect({}, frame => {

            var payloadBody = null;
            client.subscribe("/gameLobby", payload => {

                payloadBody = JSON.parse(payload.body);
                console.log("payload body " + payload.body);
                console.log("json parsed " + payloadBody)
                console.log("payload body username " + payloadBody.username);
                console.log("payload body message " + payloadBody.message);
                console.log("is array " + Array.isArray(payloadBody));
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
        client.send('/ws/userLeft', {}, JSON.stringify(activeUsersList));
        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: " left the lobby\n"}));

        client.disconnect(payload => {
            client.disconnect();
        });
    }
}

/*function sendChatMessage(){

        let authenticatedUserTag = document.getElementById("currentUsername");
        let authenticatedUserName = authenticatedUserTag.innerHTML;
        let textOutput = document.getElementById("chatOutput");
        textOutput.value += authenticatedUserName + ":" + textToSend;

        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: textToSend}));
//        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName}));

}*/

function submitMsgOnEnter(){

    let textInput = document.getElementById("chatInput");
    chatText = textInput.value;
//
//    let textOutput = document.getElementById("chatOutput");
//    textOutput.value += authenticatedUserName + ":" + chatText;

    if(event.which === 13){

        client.send('/ws/sendToChat', {}, JSON.stringify({username: authenticatedUserName, message: chatText}));

        event.target.form.dispatchEvent(new Event("submit", {cancelable: true}));
        event.preventDefault();
        textInput.value = "";
        chatText = "";
    }
}

function receiveMessage(payloadBody){
//    let authenticatedUserTag = document.getElementById("currentUsername");
//    let authenticatedUserName = authenticatedUserTag.innerHTML;
//    let textInput = document.getElementById("chatInput");
//    chatText = textInput.value;
//    chatText = document.chatForm.chatInput.value;
//    console.log("input text: " + payloadBody.message)

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