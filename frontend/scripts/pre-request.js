var departments;
var employees;

fetch('http://localhost:8080/report/get-departments', {mode : 'cors'})
    .then(function (response) {
        return response.json()
    })
    .then(function (data) {
        console.log('data', data)
    })

fetch('http://localhost:8080/report/get-employees', {mode : 'no-cors'})
    .then(function (response) {
        return response.json()
    })
    .then(function (data) {
        console.log('data', data)
    })

console.log(departments)
console.log(employees)

