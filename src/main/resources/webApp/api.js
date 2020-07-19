

function nowHTML() {
    return "<span class=\"badge badge-secondary\">" + new Date().toISOString() + "_UTC</span>";
}

function runTest() {
    let result = document.querySelector('#storedTasks');

    function newLog(t) {
        let settings = "s: " + t.shutterSpeed + ", i: " + t.iso + ", a: " + t.aperture + ", ev: " + t.ev
        let testClass = ((t.test) ? "test-task" : "")
        return "<li class=\"list-group-item " + testClass + "\">" + nowHTML() + "<span class=\"badge badge-primary\">" + settings + "<span></li>";
    }

    let xhr = new XMLHttpRequest();
    let url = "/timelapse/test";
    xhr.open("GET", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let data = JSON.parse(xhr.responseText);
            data.forEach(function(t){
                result.innerHTML = newLog(t) + result.innerHTML;
            })
        }
    };
    result.innerHTML = result.innerHTML + "<li class=\"list-group-item\">" + nowHTML() + "<span class=\"badge badge-success\">Commence test run<span></li>";
    xhr.send();
}

const rateFetchCaptured = 1000;

function getTimeForNextFetchCaptured() {
    let now = new Date();
    return new Date(now - rateFetchCaptured).toISOString()
}

function getCapturedTasks() {
    let result = document.querySelector('#executedTasks');

    function timeHTML(time) {
        return "<span class=\"badge badge-secondary\">" + time + "_UTC</span>"
    }

    function newLog(c) {
        let testClass = ((c.test) ? "test-task" : "");
        if (c.error) {
            let msg = "<span class=\"badge badge-danger\">" + c.error + "</span>"
            let suggestion = "<p>" + c.suggestion + "</p>"
            return "<li class=\"list-group-item " + testClass + "\">" + timeHTML(c.timestamp) + msg + suggestion + "</li>";
        } else {
            let settings = "s: " + c.shutterSpeed + ", i: " + c.iso + ", a: " + c.aperture + ", ev: " + c.ev
            return "<li class=\"list-group-item " + testClass + "\">" + timeHTML(c.timestamp) + "<span class=\"badge badge-primary\">" + settings + "<span></li>";
        }
    }

    let xhr = new XMLHttpRequest();
    let url = "/timelapse/captured/" + getTimeForNextFetchCaptured();
    xhr.open("GET", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let data = JSON.parse(xhr.responseText);

            if (data.length > 0) {
                data.forEach(function(c) {
                    result.innerHTML = newLog(c) + result.innerHTML;
                })
            }
        }
    };
    xhr.send();
}

setInterval(getCapturedTasks, rateFetchCaptured);