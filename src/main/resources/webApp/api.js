

function runTest(){
    let result = document.querySelector('#result');
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
            let data = JSON.parse(xhr.responseText);
            let now = "<span class=\"badge badge-secondary\">" + new Date().toISOString() + "_UTC</span>"
            if (data.error) {
                let msg = "<span class=\"badge badge-danger\">" + data.error.msg + "</span>"
                let suggestion = "<p>" + data.error.suggestion + "</p>"
                result.innerHTML = result.innerHTML + "<li class=\"list-group-item\">" + now + msg + suggestion + "</li>";
            } else {
                result.innerHTML = result.innerHTML + "<li class=\"list-group-item\">" + now + "<span class=\"badge badge-primary\">" + data.result + "<span></li>";
            }
        }
    };

    xhr.send();
}