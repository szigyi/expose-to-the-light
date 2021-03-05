
function addNewLine() {
    let settingsContainer = document.querySelector('#settings-container');
    let settings = document.querySelector('#settings');
    let cloned = settings.cloneNode(true);
    settingsContainer.appendChild(cloned);
}