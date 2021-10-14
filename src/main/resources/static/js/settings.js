    /* globals from th:inline script tag in settings.html

            const textDiffEasy = [[#{Tooltip.difficulty.easy}]];
            const textDiffNormal = [[#{Tooltip.difficulty.normal}]];
            const textDiffHard = [[#{Tooltip.difficulty.hard}]];
            const textStartingPlayer1 = [[#{Tooltip.startingPlayer.player1}]];
            const textStartingPlayer2 = [[#{Tooltip.startingPlayer.player2}]];
            const textStartingPlayerRandom = [[#{Tooltip.startingPlayer.random}]];
            const textShipShapeClassic = [[#{Tooltip.shipShape.classic}]];
            const textShipShapeAny = [[#{Tooltip.shipShape.anyDirection}]];

    */

    window.onload = () => {
        resetTimer();
        idleLogout();
        setIconsInteractive();
    }

    function setIconsInteractive(){
    var icons = document.querySelectorAll(".glyphicon");
    icons.forEach(icon => {
        $(icon).on({
                        mouseenter: function() {

                                this.style.color = "#1e90ff";
                                this.style.cursor = "pointer";

                        },
                        mouseleave: function() {

                                this.style.color = "white";
                                this.style.cursor = "default";

                        }
                });
        });
    }

    function setTooltipInfo(option, parentDiv){

        let textToSet;
        switch(option){
            case "EASY" :
                textToSet = textDiffEasy;
                break;
            case "NORMAL" :
                textToSet = textDiffNormal;
                break;
            case "HARD" :
                textToSet = textDiffHard;
                break;
            case "PLAYER1" :
                textToSet = textStartingPlayer1;
                break;
            case "PLAYER2" :
                textToSet = textStartingPlayer2;
                break;
            case "RANDOM" :
                textToSet = textStartingPlayerRandom;
                break;
            case "CLASSIC" :
                textToSet = textShipShapeClassic;
                break;
            case "ANY_DIRECTION" :
                textToSet = textShipShapeAny;
                break;
        }
        textToSet.replaceAll('. ', '\u000d');

        let icon = document.querySelector("#" + parentDiv + " .glyphicon");
        icon.remove();
        let newIcon = document.createElement('i');
        newIcon.classList.add('glyphicon');
        newIcon.classList.add('glyphicon-question-sign');
        newIcon.style.fontSize = "15px";
        newIcon.style.color = "white";
        newIcon.setAttribute('data-toggle', 'tooltip');
        newIcon.setAttribute('data-html', 'true');
        newIcon.setAttribute('title', textToSet);
        document.querySelector("#" + parentDiv).appendChild(newIcon);

        setIconsInteractive();
    }