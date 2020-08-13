
function getLastCapturedImage(){
    let img = document.querySelector('#last_captured_image');

    let xhr = new XMLHttpRequest();
    xhr.open("GET", "/last-captured-image", true);
    xhr.setRequestHeader("Content-Type", "application/json");
    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            let data = JSON.parse(xhr.responseText);
            data.forEach(function(t){
                let o = document.createElement('option');
                o.value = t
                o.innerHTML = t
                dropdown.appendChild(o);
            })
        }
    };
    xhr.send();
}