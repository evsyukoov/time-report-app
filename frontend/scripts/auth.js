const checkUserRequest = (redirectFromMainPage) => {

     new Promise((resolve, reject) => {
         const xhr = new XMLHttpRequest();
         xhr.open("GET", referer + "/check/user");
         xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
         xhr.setRequestHeader('Access-Control-Allow-Origin', referer)
         xhr.setRequestHeader('Access-Control-Allow-Methods', 'GET, POST, PATCH, PUT, DELETE, OPTIONS')
         xhr.withCredentials = true
         xhr.responseType = ""

         xhr.onload = () => {
             if (xhr.status === 401) {
                 console.log("Пользователь не авторизован")
                 window.location.href = referer + "/login.html"
             } else {
                 console.log("Пользователь уже получил cookie и может работать с админ-панелью")
                 if (redirectFromMainPage) {
                     window.location.href = referer + "/admin.html"
                 }
             }
         };

         xhr.onerror = () => {
             reject('Something went wrong!');
         };

         xhr.send( );
     });
    return true
};

window.addEventListener("load", () => {
    function sendData() {

        const xhr = new XMLHttpRequest();

        xhr.open("POST", referer + "/login");
        xhr.withCredentials = true
        xhr.setRequestHeader('Access-Control-Allow-Origin', referer)

        const fd = new FormData();
        const input = form.getElementsByClassName("input-data")
        fd.append("username", input.item(0).value)
        fd.append("password", input.item(1).value)

        xhr.onreadystatechange = function(oEvent) {
            if (xhr.readyState === 4) {
                if (xhr.status !== 200) {
                    swal("Неверное имя пользователя или пароль!",{
                        icon: "error",
                    });
                    return
                }
                console.log("Authorize was successfully")
                window.location.href = referer + "/admin.html"
            }
        };

        xhr.send(fd);
    }

    const form = document.getElementById("formLogin")

    // Add 'submit' event handler
    form.addEventListener("submit", (event) => {
        event.preventDefault();

        sendData();
    });
});
