import {OrderDetails} from "../model/OrderDetails.js";
const customer_servletUrl = 'http://localhost:2526/pos/customers';
const item_servletUrl = 'http://localhost:2526/pos/items';
const order_servletUrl = 'http://localhost:2526/pos/orders';
const detail_servletUrl = 'http://localhost:2526/pos/details';
const orderIdPattern = /^[O]-\d{4}$/;
const discountPattern = /^(?:0|[1-9]\d?)(?:\.\d{1,2})?$/;
let order_row_index = null;
let item_row_index = null;
let sub_total = 0.00;
var temp_cart_db = [];
$("#order_id").on('keypress' , ()=> { $("#customer_select").focus(); });
$("#cash").on('keypress' , ()=> { $("#discount").focus(); });

// toastr error message
function showError(message) {
    toastr.error(message, 'Oops...', {
        "closeButton": true,
        "progressBar": true,
        "positionClass": "toast-top-center",
        "timeOut": "2500"
    });
}

// reset the invoice form
const clear_form1 = () => {
    generateNextId().then(r => {});
    let today = new Date();
    let year = today.getFullYear();
    let month = today.getMonth() + 1;
    let day = today.getDate();
    let formattedDate = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
    $("#date").val(formattedDate);
    $("#order_customer_name").val("");
    $("#order_total").val("");
    sub_total = 0.00;
    loadOrderData();
}

// reset the payment form
const clear_form3 = () => {
    $("#cash").val("");
    $("#discount").val("");
    $("#balance").val("");
    document.getElementById("total").innerHTML = "Total : Rs. 0.00";
    document.getElementById("subTotal").innerHTML = "SubTotal : Rs. 0.00";
}

// check availability of the ID
async function isAvailableID(order_id) {
    const urlWithParams = new URL(`${order_servletUrl}?order_id=${order_id}&search_term=use_only_id`);
    try {
        const response = await fetch(urlWithParams, {method: 'GET',});
        return response.status !== 404;
    } catch (error) {
        console.error('Error:', error);
    }
}

// check availability of the ID in temp_cart
function isAvailableForUpdate(order_id) {
    let order_detail = temp_cart_db.find(order_detail => order_detail.order_id === order_id);
    return !!order_detail;
}

// check availability of the item code in temp_cart
function isAvailableCode(order_id, item_code) {
    let order_detail = temp_cart_db.find(order_detail => order_detail.order_id === order_id && order_detail.item_code === item_code);
    return !!order_detail;
}

// get next order_id
async function generateNextId() {
    const urlWithParams = new URL(`${order_servletUrl}?order_id=get_next_id&search_term=get_next_id`);
    try {
        const response = await fetch(urlWithParams, {method: 'GET',});
        if (response.ok) {
            const data = await response.json();
            const nextOrderID = data.next_order_id;
            console.log("Next Order ID: ", nextOrderID);
            $("#order_id").val(nextOrderID);
        } else { console.error(`HTTP error! Status: ${response.status}`);}
    } catch (error) { console.error('Error: ', error);}
}

// load all customer id's to the select box
const loadCustomers = () => {
    let title = $('<option>', { text: '-Set Customer-', value: 'title' });
    $("#customer_select").append(title);

    const urlWithParams = new URL(`${customer_servletUrl}?customer_id=get_all&search_term=get_all`);
    fetch(urlWithParams, { method: 'GET', })
    .then(response => {
        if (!response.ok) { throw new Error(`HTTP error! Status: ${response.status}`); }
        return response.json();
    })
    .then(data => {
        if (Array.isArray(data)) {
            console.log('Response customer id data: ', data);
            data.forEach(customer => {
                let option = $('<option>', { text: customer.customer_id, value: customer.customer_id });
                $("#customer_select").append(option);
            });
        } else { console.error('Error: Expected JSON array, but received: ', data); }
    })
    .catch(error => { console.error('Error:', error);});
};

// load all item codes to the select box
const loadItems = () => {
    let title = $('<option>', { text: '-Select Item-', value: 'title' });
    $("#item_select").append(title);

    const urlWithParams = new URL(`${item_servletUrl}?item_code=get_all&search_term=get_all`);
    fetch(urlWithParams, { method: 'GET', })
    .then(response => {
        if (!response.ok) { throw new Error(`HTTP error! Status: ${response.status}`); }
        return response.json();
    })
    .then(data => {
        if (Array.isArray(data)) {
            console.log('Response item codes data: ', data);
            data.forEach(item => {
                let option = $('<option>', { text: item.item_code, value: item.item_code });
                $("#item_select").append(option);
            });
        } else { console.error('Error: Expected JSON array, but received:', data); }
    })
    .catch(error => { console.error('Error:', error);});
};

// load get items to the cart table
const loadCartItemData = () => {
    $('#order_item_tbl_body').empty();
    temp_cart_db.map((cartItemData, index) => {
        let unitPrice = parseFloat(cartItemData.unit_price);
        let quantity = parseInt(cartItemData.get_qty);
        let total = unitPrice * quantity;
        let record = `<tr>
                        <td class="item_code">${cartItemData.item_code}</td>
                        <td class="description">${cartItemData.description}</td>
                        <td class="unit_price">${unitPrice}</td>
                        <td class="get_qty">${quantity}</td>
                        <td class="item_total">${total}</td>
                      </tr>`;
        $("#order_item_tbl_body").append(record);
    });
};

// load all order info to the table
const loadOrderData = () => {
    $('#order_tbl_body').empty();
    const urlWithParams = new URL(`${order_servletUrl}?order_id=get_all&search_term=get_all`);
    fetch(urlWithParams, {method: 'GET',})
    .then(response => {
        if (!response.ok) {throw new Error(`HTTP error! Status: ${response.status}`);}
        return response.json();
    })
    .then(data => {
        if (Array.isArray(data)) {
            console.log('Response order info data: ', data);
            $('#order_tbl_body').empty();
            data.forEach(order => {
                let record = `<tr><td class="order_id">${order.order_id}</td><td class="date">${order.date}</td>
                <td class="customer_id">${order.customer_id}</td><td class="discount">${order.discount}</td><td class="order_total">${order.total}</td></tr>`;
                $("#order_tbl_body").append(record);
            });
        } else { console.error('Error: Expected JSON array, but received: ', data);}
    })
    .catch(error => { console.error('Error:', error);});
};

// reset order form
$("#order_btns>button[type='button']").eq(2).on("click", () => {
    $('#customer_select').empty();
    loadOrderData();
    loadCustomers();
    clear_form1();
    clear_form3();
    temp_cart_db = [];
    loadCartItemData();
});

// reset cart
$("#cart_btns>button[type='reset']").eq(0).on("click", () => {
    $('#item_select').empty();
    loadItems();
});

// add item to the cart
$("#cart_btns>button[type='button']").eq(0).on("click", () => {
    let order_id = $("#order_id").val();
    let order_item_code = $("#order_item_code").val();
    let order_item_description = $("#order_item_description").val();
    let total_item_qty = $("#total_item_qty").val();
    let order_item_unit_price = $("#order_item_unit_price").val();
    let order_get_item_qty = $("#order_get_item_qty").val();
    if(order_id) {
        if (orderIdPattern.test(order_id)) {
            if (order_item_code && order_get_item_qty) {
                if(parseInt(order_get_item_qty)<=parseInt(total_item_qty) && parseInt(order_get_item_qty)>0) {
                    if(isAvailableCode(order_id, order_item_code)) {
                        let order_detail = temp_cart_db.find(order_detail =>
                            order_detail.order_id === order_id && order_detail.item_code === order_item_code);
                        let replace_index = temp_cart_db.findIndex(item =>
                            item.order_id === order_id && item.item_code === order_item_code);
                        let inCart_count = parseInt(order_detail.get_qty);
                        let new_count = inCart_count + parseInt(order_get_item_qty);
                        temp_cart_db[replace_index] = new OrderDetails(order_id, order_item_code, order_item_description, order_item_unit_price, new_count);
                        sub_total -= (order_item_unit_price * inCart_count);
                        sub_total += order_item_unit_price * new_count;
                        document.getElementById("subTotal").innerHTML = "Sub Total : Rs. "+sub_total;
                        $("#cart_btns>button[type='reset']").click();
                        loadCartItemData();
                    } else {
                        let cart_item_obj =
                            new OrderDetails(order_id, order_item_code, order_item_description, order_item_unit_price, order_get_item_qty);
                        temp_cart_db.push(cart_item_obj);
                        sub_total += order_item_unit_price * order_get_item_qty;
                        document.getElementById("subTotal").innerHTML = "Sub Total : Rs. "+sub_total;
                        $("#cart_btns>button[type='reset']").click();
                        loadCartItemData();
                    }
                } else { showError('Invalid Quantity!'); }
            } else { showError('Fields can not be empty!'); }
        } else { showError('Invalid Order ID format!'); }
    } else { showError('Order ID can not be empty!'); }
});

// remove item from the cart
$("#cart_btns>button[type='button']").eq(1).on("click", () => {
    let order_id = $("#order_id").val();
    let order_item_code = $("#order_item_code").val();
    let order_item_description = $("#order_item_description").val();
    let order_item_unit_price = $("#order_item_unit_price").val();
    let order_get_item_qty = parseInt($("#order_get_item_qty").val());
    if(order_id) {
        if (orderIdPattern.test(order_id)) {
            if (order_item_code && order_get_item_qty) {
                if(isAvailableCode(order_id, order_item_code)) {
                    let cart_item_data = temp_cart_db.find(order_detail =>
                        order_detail.order_id === order_id && order_detail.item_code === order_item_code);
                    let remove_index = temp_cart_db.findIndex(order_detail =>
                        order_detail.order_id === order_id && order_detail.item_code === order_item_code);
                    if (cart_item_data) {
                        if (order_get_item_qty === parseInt(cart_item_data.get_qty)) {
                            temp_cart_db[remove_index] =
                                new OrderDetails(order_id, order_item_code, order_item_description, order_item_unit_price, 0);
                            sub_total -= order_item_unit_price * order_get_item_qty;
                            document.getElementById("subTotal").innerHTML = "Sub Total : Rs. "+sub_total;
                            $("#cart_btns>button[type='reset']").click();
                            loadCartItemData();
                        } else if(order_get_item_qty < cart_item_data.get_qty && order_get_item_qty > 0) {
                            let inCart_count = parseInt(cart_item_data.get_qty);
                            let new_count = inCart_count - order_get_item_qty;
                            temp_cart_db[remove_index] =
                                new OrderDetails(order_id, order_item_code, order_item_description, order_item_unit_price, new_count);
                            sub_total -= (order_item_unit_price * inCart_count);
                            sub_total += order_item_unit_price * new_count;
                            document.getElementById("subTotal").innerHTML = "Sub Total : Rs. "+sub_total;
                            $("#cart_btns>button[type='reset']").click();
                            loadCartItemData();
                        } else { showError('Invalid Quantity!'); }
                    } else { showError('This item is not available to remove!'); }
                } else { showError('This Item is not available in this order!'); }
            } else { showError('Fields can not be empty!'); }
        } else { showError('Invalid Order ID format!'); }
    } else { showError('Order ID can not be empty!'); }
});

// when change the customer_select
$("#customer_select").on("change", async function () {
    let selectedCustomerID = $(this).val();
    const urlWithParams = new URL(`${customer_servletUrl}?customer_id=${selectedCustomerID}&search_term=use_only_id`);
    try {
        const response = await fetch(urlWithParams, {method: 'GET',});
        if (response.ok) {
            const data = await response.json();
            const name = data.name;
            console.log("Customer name: ", name);
            $("#order_customer_name").val(name);
        } else { clear_form1(); console.error(`HTTP error! Status: ${response.status}`);}
    } catch (error) { console.error('Error:', error);}
});

// when change the item_select
$("#item_select").on("change", async function () {
    $("#order_get_item_qty").val("");
    let order_id = $("#order_id").val();
    let selectedItem = $(this).val();

    const urlWithParams = new URL(`${item_servletUrl}?item_code=${selectedItem}&search_term=use_only_code`);
    try {
        const response = await fetch(urlWithParams, {method: 'GET',});
        if (response.ok) {
            const data = await response.json();
            $("#order_item_code").val(data.item_code);
            $("#order_item_description").val(data.description);
            $("#order_item_unit_price").val(data.unit_price);
            let cart_item_data = temp_cart_db.find(item => item.item_code === selectedItem);
            if (cart_item_data && !await isAvailableID(order_id)) {
                let actual_item_qty = data.qty_on_hand;
                let in_cart_item_qty = cart_item_data.get_qty;
                $("#total_item_qty").val(actual_item_qty - in_cart_item_qty);
            } else { $("#total_item_qty").val(data.qty_on_hand);}
        } else { $("#cart_btns>button[type='reset']").eq(0).click();}
    } catch (error) { console.error('Error:', error);}
});

// update item quantities in the db
async function updateItemQuantities() {
    for (let i = 0; i < temp_cart_db.length; i++) {
        let itemCode = temp_cart_db[i].item_code;
        let get_qty = temp_cart_db[i].get_qty;

        const urlWithParams = new URL(`${item_servletUrl}?item_code=${itemCode}&search_term=use_only_code`);
        try {
            const response = await fetch(urlWithParams, { method: 'GET', });
            if (response.ok) {
                const data = await response.json();
                let description = data.description;
                let unit_price = data.unit_price;
                let qty_on_hand = parseInt(data.qty_on_hand);

                let real_temp_item_data = real_temp_cart_db.find(real_item => real_item.item_code === itemCode);
                let updated_qty;

                if (real_temp_item_data) {
                    let real_temp_qty = parseInt(real_temp_item_data.get_qty);
                    updated_qty = (qty_on_hand + real_temp_qty) - get_qty;
                } else {
                    updated_qty = qty_on_hand - get_qty;
                }
                // update item with updated qty
                let itemObject = {
                    item_code: itemCode,
                    description: description,
                    unit_price: unit_price,
                    qty_on_hand: updated_qty
                };
                console.log('itemObject: ', itemObject);
                let itemJSON = JSON.stringify(itemObject);
                console.log('itemJSON: ', itemJSON);
                $.ajax({
                    url: `${item_servletUrl}`,
                    type: "PUT",
                    data: itemJSON,
                    headers: {"Content-Type": "application/json"},
                    success: (res) => {
                        console.log(JSON.stringify(res));
                        console.log(itemCode+" item updated.");
                    },
                    error: (err) => { console.error(err) }
                });
            } else {console.error(`HTTP error! Status: ${response.status}`);}
        } catch (error) {console.error('Error:', error);}
    }
}

// add order details to the db
function addOrderDetails() {
    for (let i = temp_cart_db.length - 1; i >= 0; i--) {
        if(temp_cart_db[i].get_qty === 0){
            temp_cart_db.splice(i, 1);
        }
    }
    // convert the temp_cart_db array to a JSON array
    let temp_cart_db_JSON = JSON.stringify(temp_cart_db);
    console.log('temp_cart_db_JSON: ', temp_cart_db_JSON);
    // add temp_cart_db details to order details db
    $.ajax({
        url: `${detail_servletUrl}`,
        type: "POST",
        data: temp_cart_db_JSON,
        headers: {"Content-Type": "application/json"},
        success: (res) => {
            console.log(JSON.stringify(res));
            console.log("temp_cart_db details save successfully");
        },
        error: (err) => { console.error(err);}
    });
    temp_cart_db = [];
}

// remove order details from the db
async function removeOrderDetails(order_id) {
    const urlWithParams = new URL(`${detail_servletUrl}?order_id=${order_id}`);
    try {
        const response = await fetch(urlWithParams, {method: 'DELETE',});
        if (response.ok) {console.log("order details removed successfully."); }
        else { console.log("order details not removed."); }
    } catch (error) {console.error('Error: ', error);}
}

// recover & update item qty in the db
function recoverItems(order_id) {
    // get order details with this order_id
    const urlWithParams = new URL(`${detail_servletUrl}?order_id=${order_id}`);
    fetch(urlWithParams, {method: 'GET',})
    .then(response => {
        if (!response.ok) { throw new Error(`HTTP error! Status: ${response.status}`);}
        return response.json();
    })
    .then(async data => {
        if (Array.isArray(data)) {
            console.log('Response order details data: ', data);
            // Iterate through the array and handle each detail as needed
            for (const order_detail of data) {
                // Handle each detail in the array
                console.log('Order Detail: ', order_detail);
                let item_code = order_detail.item_code;
                let recover_qty = parseInt(order_detail.get_qty);

                // search is available item
                const url = new URL(`${item_servletUrl}?item_code=${item_code}&search_term=use_only_code`);
                try {
                    const response = await fetch(url, {method: 'GET',});
                    if (response.ok) {
                        const data = await response.json();
                        let description = data.description;
                        let unit_price = data.unit_price;
                        let qty_on_hand = parseInt(data.qty_on_hand);
                        let updated_qty = qty_on_hand + recover_qty;

                        // update item db with recover counts
                        let itemObject = {
                            item_code: item_code,
                            description: description,
                            unit_price: unit_price,
                            qty_on_hand: updated_qty
                        };
                        console.log('itemObject: ', itemObject);
                        let itemJSON = JSON.stringify(itemObject);
                        console.log('itemJSON: ', itemJSON);
                        $.ajax({
                            url: `${item_servletUrl}`,
                            type: "PUT",
                            data: itemJSON,
                            headers: {"Content-Type": "application/json"},
                            success: (res) => {
                                console.log(JSON.stringify(res));
                                console.log("successfully updated the item");
                            },
                            error: (err) => { console.error(err)}
                        });
                    }
                } catch (error) {console.error('Error:', error);}
            }
        } else {console.error('Error: Expected JSON array, but received:', data);}
    })
    .catch(error => {console.error('Error:', error);});
}

//  place order
$("#btn_place_order").on("click", async () => {
    let order_id = $("#order_id").val();
    let date = $("#date").val();
    let order_customer_id = $("#customer_select").val();
    let order_customer_name = $("#order_customer_name").val();
    let cash = $("#cash").val();
    let discount = $("#discount").val();
    let total = sub_total - (sub_total * discount / 100);
    if (order_id) {
        if (orderIdPattern.test(order_id)) {
            if (!(await isAvailableID(order_id))) {
                if (order_customer_name) {
                    if (discountPattern.test(discount)) {
                        if (total > 0) {
                            if (cash >= total) {
                                let orderObject = {
                                    order_id: order_id,
                                    date: date,
                                    customer_id: order_customer_id,
                                    discount: discount,
                                    total: total
                                };
                                console.log('orderObject: ', orderObject);
                                let orderJSON = JSON.stringify(orderObject);
                                console.log('orderJSON: ', orderJSON);
                                $.ajax({
                                    url: `${order_servletUrl}`,
                                    type: "POST",
                                    data: orderJSON,
                                    headers: {"Content-Type": "application/json"},
                                    success: async (res) => {
                                        console.log(JSON.stringify(res));
                                        Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Saved!', showConfirmButton: false, timer: 2000});
                                        await updateItemQuantities();
                                        addOrderDetails();
                                        $("#order_btns>button[type='button']").eq(2).click();
                                        $("#cart_btns>button[type='reset']").eq(0).click();
                                        loadOrderData();
                                        clear_form3();
                                        $('#order_item_tbl_body').empty();
                                    },
                                    error: (err) => { console.error(err);}
                                });
                            } else {showError('Insufficient cash!');}
                        } else {showError('Invalid total!');}
                    } else {showError('Invalid Discount input!');}
                } else {showError('Enter valid Customer ID!');}
            } else {showError('This Order ID is already exist!');}
        } else {showError('Invalid Order ID format!');}
    } else {showError('Order ID can not be empty!');}
});

// update order
$("#order_btns>button[type='button']").eq(0).on("click", async () => {
    let order_id = $("#order_id").val();
    let date = $("#date").val();
    let order_customer_id = $("#customer_select").val();
    let order_customer_name = $("#order_customer_name").val();
    let discount = $("#discount").val();
    let total = sub_total - (sub_total * discount / 100);
    if (order_id) {
        if (orderIdPattern.test(order_id)) {
            if ((await isAvailableID(order_id))) {
                if (order_customer_name) {
                    if (discountPattern.test(discount)) {
                        if (total > 0) {
                            if (isAvailableForUpdate(order_id)) {
                                let orderObject = {
                                    order_id: order_id,
                                    date: date,
                                    customer_id: order_customer_id,
                                    discount: discount,
                                    total: total
                                };
                                console.log('orderObject: ', orderObject);
                                let orderJSON = JSON.stringify(orderObject);
                                console.log('orderJSON: ', orderJSON);
                                $.ajax({
                                    url: `${order_servletUrl}`,
                                    type: "PUT",
                                    data: orderJSON,
                                    headers: {"Content-Type": "application/json"},
                                    success: async (res) => {
                                        console.log(JSON.stringify(res));
                                        Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Updated!', showConfirmButton: false, timer: 2000});
                                        await removeOrderDetails(order_id);
                                        await updateItemQuantities();
                                        addOrderDetails();
                                        $("#order_btns>button[type='button']").eq(2).click();
                                        $("#cart_btns>button[type='reset']").eq(0).click();
                                        loadOrderData();
                                        clear_form3();
                                        $('#order_item_tbl_body').empty();
                                    },
                                    error: (err) => { console.error(err);}
                                });
                            } else {showError('No details to update!');}
                        } else {showError('Invalid total!');}
                    } else {showError('Invalid Discount input!');}
                } else {showError('Enter valid Customer ID!');}
            } else {showError('This Order ID is not exist!');}
        } else {showError('Invalid Order ID format!');}
    } else {showError('Order ID can not be empty!');}
});

// delete order
$("#order_btns>button[type='button']").eq(1).on("click", async () => {
    let order_id = $("#order_id").val();
    if (order_id) {
        if (orderIdPattern.test(order_id)) {
            if ((await isAvailableID(order_id))) {
                Swal.fire({
                    width: '300px', title: 'Delete Order', text: "Are you sure you want to permanently remove this order?",
                    icon: 'warning', showCancelButton: true, confirmButtonColor: '#3085d6', cancelButtonColor: '#d33', confirmButtonText: 'Yes, delete!'
                }).then(async (result) => {
                    if (result.isConfirmed) {
                        await recoverItems(order_id);
                        await removeOrderDetails(order_id);
                        let orderObject = { order_id: order_id };
                        console.log('orderObject: ', orderObject);
                        let orderJSON = JSON.stringify(orderObject);
                        console.log('orderJSON: ', orderJSON);
                        $.ajax({
                            url: `${order_servletUrl}`,
                            type: "DELETE",
                            data: orderJSON,
                            headers: {"Content-Type": "application/json"},
                            success: async (res) => {
                                console.log(JSON.stringify(res));
                                Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Deleted!', showConfirmButton: false, timer: 2000});
                                $("#order_btns>button[type='button']").eq(2).click();
                                loadOrderData();
                            },
                            error: (err) => {console.error(err)}
                        });
                    }
                })
            } else {showError('This Order ID is not exist!');}
        } else {showError('Invalid Order ID format!');}
    } else {showError('Order ID can not be empty!');}
});

// calculate discount & balance
const inputElement = document.getElementById('discount');
inputElement.addEventListener('input', function (event) {
    let discount = event.target.value;
    if(discountPattern.test(discount)) {
        let total = sub_total - (sub_total * discount/100);
        document.getElementById("total").innerHTML = "Total : Rs. "+total;
        let cash = $("#cash").val();
        if(cash>total) {
            let balance = cash - total;
            $("#balance").val(balance);
            $("#order_total").val(total);
        } else { Swal.fire({width: '300px', icon: 'warning', title: 'Oops...', text: 'Insufficient cash for get balance!'}); }
    } else { showError('Invalid Discount input!'); }
});

// retrieve by table click
$("#order_item_tbl_body").on("click", "tr", function() {
    item_row_index = $(this).index();
    let item_id = $(this).find(".item_code").text();
    let description = $(this).find(".description").text();
    let unit_price = $(this).find(".unit_price").text();
    let get_qty = $(this).find(".get_qty").text();
    $("#order_item_code").val(item_id);
    $("#order_item_description").val(description);
    $("#order_item_unit_price").val(unit_price);
    $("#order_get_item_qty").val(get_qty);
});

// retrieve item by table click
$("#order_tbl_body, #order_search_tbl_body").on("click", "tr", async function () {
    order_row_index = $(this).index();
    let order_id = $(this).find(".order_id").text();
    let date = $(this).find(".date").text();
    let customer_id = $(this).find(".customer_id").text();
    let customer_name;

    const urlWithParams = new URL(`${customer_servletUrl}?customer_id=${customer_id}&search_term=use_only_id`);
    try {
        const response = await fetch(urlWithParams, {method: 'GET',});
        if (response.ok) { const data = await response.json();customer_name = data.name; }
        else { console.error(`HTTP error! Status: ${response.status}`); }
    } catch (error) { console.error('Error:', error); }

    let discount = $(this).find(".discount").text();
    let total = $(this).find(".order_total").text();
    sub_total = total / (100 - discount) * 100;
    document.getElementById("total").innerHTML = "Total : Rs. " + total;
    document.getElementById("subTotal").innerHTML = "Sub Total : Rs. " + sub_total;
    await getOrderDetailsToTemp(order_id);
    loadCartItemData();
    $("#order_id").val(order_id);
    $("#date").val(date);
    $("#customer_select").val(customer_id);
    $("#order_customer_name").val(customer_name);
    $("#discount").val(discount);
    $("#order_total").val(total);
});

// get order details to real_temp_cart_db
var real_temp_cart_db = [];
async function getOrderDetailsToRealTemp(order_id) {
    real_temp_cart_db = [];
    const urlWithParams = new URL(`${detail_servletUrl}?order_id=${order_id}`);
    try {
        const response = await fetch(urlWithParams, { method: 'GET' });
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        if (Array.isArray(data)) {
            console.log('Response order details data:', data);
            real_temp_cart_db = data.map(item => ({ ...item }));
            console.log('real_temp_cart_db:', real_temp_cart_db);
        }
    } catch (error) { console.error('Error:', error);}
}

// get order details to temp_cart_db
async function getOrderDetailsToTemp(order_id) {
    await getOrderDetailsToRealTemp(order_id);
    temp_cart_db = [...real_temp_cart_db];
    console.log('temp_cart_db:', temp_cart_db);
}

// search orders by using order_id or customer_id
$('#order_search_box').on('input', () => {
    let search_term = $('#order_search_box').val();
    if(search_term){
        $('#order_search_tbl_body').empty();
        const urlWithParams = new URL(`${order_servletUrl}?order_id=use_search_term&search_term=${search_term}`);
        fetch(urlWithParams, { method: 'GET', })
        .then(response => {
            if (!response.ok) { throw new Error(`HTTP error! Status: ${response.status}`);}
            return response.json();
        })
        .then(data => {
            if (Array.isArray(data)) {
                console.log('Response order data: ', data);
                data.forEach(order => {
                    let record = `<tr><td class="order_id">${order.order_id}</td><td class="date">${order.date}</td>
                    <td class="customer_id">${order.customer_id}</td><td class="order_total">${order.total}</td></tr>`;
                    $("#order_search_tbl_body").append(record);
                });
            } else { console.error('Error: Expected JSON array, but received:', data);}
        })
        .catch(error => {console.error('Error:', error);});
    }else{ $('#order_search_tbl_body').empty();}
});

// v2 concise & finalize