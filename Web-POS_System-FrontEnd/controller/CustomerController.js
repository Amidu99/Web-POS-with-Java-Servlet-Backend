const servletUrl = 'http://localhost:2526/pos/customers';
const customerIdPattern = /^[C]-\d{4}$/;
const namePattern = /^[a-zA-Z '.-]{4,}$/;
const addressPattern = /^[a-zA-Z0-9 ',.-]{5,}$/;
const salaryPattern = /^(?:[1-9]\d{0,5})(?:\.\d{1,2})?$/;
let row_index = null;
$("#customer_id").on('keypress' , ()=> { $("#customer_name").focus(); });
$("#customer_name").on('keypress' , ()=> { $("#customer_address").focus(); });
$("#customer_address").on('keypress' , ()=> { $("#customer_salary").focus();});

// toastr error message
function showError(message) {
    toastr.error(message, 'Oops...', {
        "closeButton": true,
        "progressBar": true,
        "positionClass": "toast-top-center",
        "timeOut": "2500"
    });
}

// check availability of the ID
async function isAvailableID(customer_id) {
    const urlWithParams = new URL(`${servletUrl}?customer_id=${customer_id}&search_term=use_only_id`);
    try {
        const response = await fetch(urlWithParams, {method: 'GET',});
        return response.status !== 404;
    } catch (error) { console.error('Error:', error);}
}

// save customer
$("#customer_btns>button[type='button']").eq(0).on("click", async () => {
    let customer_id = $("#customer_id").val();
    let name = $("#customer_name").val();
    let address = $("#customer_address").val();
    let salary = $("#customer_salary").val();
    if (customer_id && name && address && salary) {
        if (customerIdPattern.test(customer_id)) {
            if (!(await isAvailableID(customer_id))) {
                if (namePattern.test(name)) {
                    if (addressPattern.test(address)) {
                        if (salaryPattern.test(salary)) {
                            // Create a JS object
                            let customerObject = {
                                customer_id: customer_id,
                                name: name,
                                address: address,
                                salary: salary
                            };
                            console.log('customerObject: ', customerObject);
                            // Convert the JS object to a JSON
                            let customerJSON = JSON.stringify(customerObject);
                            console.log('customerJSON: ', customerJSON);
                            // AJAX - JQuery
                            $.ajax({
                                url: `${servletUrl}`,
                                type: "POST",
                                data: customerJSON,
                                headers: {"Content-Type": "application/json"},
                                success: (res) => {
                                    console.log(JSON.stringify(res));
                                    Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Saved!', showConfirmButton: false, timer: 2000});
                                    $("#customer_btns>button[type='button']").eq(3).click();
                                    loadCustomerData();
                                },
                                error: (err) => { console.error(err);}
                            });
                        } else { showError('Invalid salary input!');}
                    } else { showError('Invalid address input!');}
                } else { showError('Invalid name input!');}
            } else { showError('This ID is already exist!');}
        } else { showError('Invalid customer ID format!');}
    } else { showError('Fields can not be empty!');}
});

// update customer
$("#customer_btns>button[type='button']").eq(1).on("click", async () => {
    let customer_id = $("#customer_id").val();
    let name = $("#customer_name").val();
    let address = $("#customer_address").val();
    let salary = $("#customer_salary").val();
    if (customer_id && name && address && salary) {
        if (customerIdPattern.test(customer_id)) {
            if ((await isAvailableID(customer_id))) {
                if (namePattern.test(name)) {
                    if (addressPattern.test(address)) {
                        if (salaryPattern.test(salary)) {
                            let customerObject = {
                                customer_id: customer_id,
                                name: name,
                                address: address,
                                salary: salary
                            };
                            console.log('customerObject: ', customerObject);
                            let customerJSON = JSON.stringify(customerObject);
                            console.log('customerJSON: ', customerJSON);
                            $.ajax({
                                url: `${servletUrl}`,
                                type: "PUT",
                                data: customerJSON,
                                headers: {"Content-Type": "application/json"},
                                success: (res) => {
                                    console.log(JSON.stringify(res));
                                    Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Updated!', showConfirmButton: false, timer: 2000});
                                    $("#customer_btns>button[type='button']").eq(3).click();
                                    loadCustomerData();
                                },
                                error: (err) => { console.error(err);}
                            });
                        } else { showError('Invalid salary input!');}
                    } else { showError('Invalid address input!');}
                } else { showError('Invalid name input!');}
            } else { showError('This ID is not exist!');}
        } else { showError('Invalid customer ID format!');}
    } else { showError('Fields can not be empty!');}
});

// delete customer
$("#customer_btns>button[type='button']").eq(2).on("click", async () => {
    let customer_id = $("#customer_id").val();
    if (customer_id) {
        if (customerIdPattern.test(customer_id)) {
            if ((await isAvailableID(customer_id))) {
                Swal.fire({
                    width: '300px', title: 'Delete Customer',
                    text: "Are you sure you want to permanently remove this customer?",
                    icon: 'warning', showCancelButton: true, confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#d33', confirmButtonText: 'Yes, delete!'
                }).then((result) => {
                    if (result.isConfirmed) {
                        let customerObject = {customer_id: customer_id};
                        console.log('customerObject: ', customerObject);
                        let customerJSON = JSON.stringify(customerObject);
                        console.log('customerJSON: ', customerJSON);
                        $.ajax({
                            url: `${servletUrl}`,
                            type: "DELETE",
                            data: customerJSON,
                            headers: {"Content-Type": "application/json"},
                            success: (res) => {
                                console.log(JSON.stringify(res));
                                Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Deleted!', showConfirmButton: false, timer: 2000});
                                $("#customer_btns>button[type='button']").eq(3).click();
                                loadCustomerData();
                            },
                            error: (err) => { console.error(err)}
                        });
                    }
                });
            } else { showError('This ID is not exist!');}
        } else { showError('Invalid customer ID format!');}
    } else { showError('Customer ID can not be empty!');}
});

// reset customer form
$("#customer_btns>button[type='button']").eq(3).on("click", async () => {
    loadCustomerData();
    $("#customer_name").val("");
    $("#customer_address").val("");
    $("#customer_salary").val("");
    $('#customer_search_tbl_body').empty();

    const urlWithParams = new URL(`${servletUrl}?customer_id=get_next_id&search_term=get_next_id`);
    try {
        const response = await fetch(urlWithParams, { method: 'GET', });
        if (response.ok) {
            const data = await response.json();
            const nextCustomerID = data.next_customer_id;
            console.log("Next customer_id: ", nextCustomerID);
            $("#customer_id").val(nextCustomerID);
        } else { console.error(`HTTP error! Status: ${response.status}`);}
    } catch (error) { console.error('Error:', error);}
});

// load all customer details to the table
const loadCustomerData = () => {
    const urlWithParams = new URL(`${servletUrl}?customer_id=get_all&search_term=get_all`);
    fetch(urlWithParams, { method: 'GET',})
        .then(response => {
            if (!response.ok) { throw new Error(`HTTP error! Status: ${response.status}`);}
            return response.json();
        })
        .then(data => {
            if (Array.isArray(data)) {
                console.log('Response customer data: ', data);
                $('#customer_tbl_body').empty();
                data.forEach(customer => {
                    let record = `<tr><td class="customer_id">${customer.customer_id}</td><td class="name">${customer.name}</td>
                    <td class="address">${customer.address}</td><td class="salary">${customer.salary}</td></tr>`;
                    $("#customer_tbl_body").append(record);
                });
            } else { console.error('Error: Expected JSON array, but received: ', data); }
        })
    .catch(error => { console.error('Error: ', error);});
};

// retrieve customer by table click
$("#customer_tbl_body, #customer_search_tbl_body").on("click", "tr", function() {
    row_index = $(this).index();
    let customer_id = $(this).find(".customer_id").text();
    let name = $(this).find(".name").text();
    let address = $(this).find(".address").text();
    let salary = $(this).find(".salary").text();
    $("#customer_id").val(customer_id);
    $("#customer_name").val(name);
    $("#customer_address").val(address);
    $("#customer_salary").val(salary);
});

// search customers by using id, name or address
$('#customer_search_box').on('input', () => {
    let search_term = $('#customer_search_box').val();
    if(search_term){
        $('#customer_search_tbl_body').empty();
        const urlWithParams = new URL(`${servletUrl}?customer_id=use_search_term&search_term=${search_term}`);
        fetch(urlWithParams, { method: 'GET',})
        .then(response => {
            if (!response.ok) { throw new Error(`HTTP error! Status: ${response.status}`); }
            return response.json();
        })
        .then(data => {
            if (Array.isArray(data)) {
                console.log('Response customer data: ', data);
                // Iterate through the array and handle each customer as needed
                data.forEach(customer => {
                    let record = `<tr><td class="customer_id">${customer.customer_id}</td><td class="name">${customer.name}</td>
                    <td class="address">${customer.address}</td><td class="salary">${customer.salary}</td></tr>`;
                    $("#customer_search_tbl_body").append(record);
                });
            } else { console.error('Error: Expected JSON array, but received : ', data);}
        })
        .catch(error => { console.error('Error: ', error);});
    }else{ $('#customer_search_tbl_body').empty(); }
});

// v2 concise & finalize