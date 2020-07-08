

function runTest(){
//    let result = document.querySelector('#result');
//    let eventPlanner = document.querySelector('#eventPlanner');

//    let startNotification = document.querySelector('#startNotification');
//    let preNotification = document.querySelector('#preNotification');
//    let updateStartNotificationTemplate = document.querySelector('#updateStartNotificationTemplate');
//    let updateEndNotificationTemplate = document.querySelector('#updateEndNotificationTemplate');
//    let endNotification = document.querySelector('#endNotification');
//    let postCalculation = document.querySelector('#postCalculation');
//    let eventReference = document.querySelector('#eventReference');
//    let maxHeroPoints = document.querySelector('input[name="maxHeroPoints"]:checked');

    let xhr = new XMLHttpRequest();
    let url = "/timelapse/test";
    xhr.open("GET", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            console.log("test is done")
//            result.innerHTML = result.innerHTML + "<br>[" + document.querySelector('#startNotification').value + "," + document.querySelector('#eventReference').value + "] OK, it is stored! Well done, mate!";
//            eventPlanner.reset();
        }
    };

    xhr.send();
}