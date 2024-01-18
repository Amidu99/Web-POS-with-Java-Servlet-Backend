const servletUrl = 'http://localhost:2526/pos/items';
const itemCodePattern = /^[I]-\d{4}$/;
const descriptionPattern = /^[a-zA-Z0-9 '.-]{4,}$/;
const pricePattern = /^(?:[1-9]\d{0,4})(?:\.\d{1,2})?$/;
const qtyPattern = /^(?:0|[1-9]\d{0,4})(?:\.\d{1,2})?$/;
let row_index = null;
$("#item_code").on('keypress' , ()=> { $("#description").focus(); });
$("#description").on('keypress' , ()=> { $("#unit_price").focus(); });
$("#unit_price").on('keypress' , ()=> { $("#item_qty").focus();});

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
async function isAvailableID(item_code) {
    const urlWithParams = new URL(`${servletUrl}?item_code=${item_code}&search_term=use_only_code`);
    try {
        const response = await fetch(urlWithParams, { method: 'GET',});
        return response.status !== 404;
    } catch (error) { console.error('Error:', error);}
}

// save item
$("#item_btns>button[type='button']").eq(0).on("click", async () => {
    let item_code = $("#item_code").val();
    let description = $("#description").val();
    let unit_price = $("#unit_price").val();
    let qty_on_hand = $("#item_qty").val();
    if (item_code && description && unit_price && qty_on_hand) {
        if (itemCodePattern.test(item_code)) {
            if (!(await isAvailableID(item_code))) {
                if (descriptionPattern.test(description)) {
                    if (pricePattern.test(unit_price)) {
                        if (qtyPattern.test(qty_on_hand)) {
                            // Create a JS object
                            let itemObject = {
                                item_code: item_code,
                                description: description,
                                unit_price: unit_price,
                                qty_on_hand: qty_on_hand
                            };
                            console.log('itemObject: ', itemObject);
                            // Convert the JavaScript object to a JSON
                            let itemJSON = JSON.stringify(itemObject);
                            console.log('itemJSON: ', itemJSON);
                            $.ajax({
                                url: `${servletUrl}`,
                                type: "POST",
                                data: itemJSON,
                                headers: {"Content-Type": "application/json"},
                                success: (res) => {
                                    console.log(JSON.stringify(res));
                                    Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Saved!', showConfirmButton: false, timer: 2000});
                                    $("#item_btns>button[type='button']").eq(3).click();
                                    loadItemData();
                                },
                                error: (err) => { console.error(err);}
                            });
                        } else { showError('Invalid quantity input!'); }
                    } else { showError('Invalid price input!'); }
                } else { showError('Invalid description!'); }
            } else { showError('This Code is already exist!'); }
        } else { showError('Invalid item code format!'); }
    } else { showError('Fields can not be empty!'); }
});

// update item
$("#item_btns>button[type='button']").eq(1).on("click", async () => {
    let item_code = $("#item_code").val();
    let description = $("#description").val();
    let unit_price = $("#unit_price").val();
    let qty_on_hand = $("#item_qty").val();
    if (item_code && description && unit_price && qty_on_hand) {
        if (itemCodePattern.test(item_code)) {
            if ((await isAvailableID(item_code))) {
                if (descriptionPattern.test(description)) {
                    if (pricePattern.test(unit_price)) {
                        if (qtyPattern.test(qty_on_hand)) {
                            let itemObject = {
                                item_code: item_code,
                                description: description,
                                unit_price: unit_price,
                                qty_on_hand: qty_on_hand
                            };
                            console.log('itemObject: ', itemObject);
                            let itemJSON = JSON.stringify(itemObject);
                            console.log('itemJSON: ', itemJSON);
                            $.ajax({
                                url: `${servletUrl}`,
                                type: "PUT",
                                data: itemJSON,
                                headers: {"Content-Type": "application/json"},
                                success: (res) => {
                                    console.log(JSON.stringify(res));
                                    Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Updated!', showConfirmButton: false, timer: 2000});
                                    $("#item_btns>button[type='button']").eq(3).click();
                                    loadItemData();
                                },
                                error: (err) => { console.error(err)}
                            });
                        } else {showError('Invalid quantity input!');}
                    } else {showError('Invalid price input!');}
                } else {showError('Invalid description!');}
            } else {showError('This Code is not exist!');}
        } else {showError('Invalid item code format!');}
    } else {showError('Fields can not be empty!');}
});

// delete item
$("#item_btns>button[type='button']").eq(2).on("click", async () => {
    let item_code = $("#item_code").val();
    if (item_code) {
        if (itemCodePattern.test(item_code)) {
            if ((await isAvailableID(item_code))) {
                Swal.fire({
                    width: '300px', title: 'Delete Item', text: "Are you sure you want to permanently remove this item?",
                    icon: 'warning', showCancelButton: true, confirmButtonColor: '#3085d6', cancelButtonColor: '#d33', confirmButtonText: 'Yes, delete!'
                }).then((result) => {
                    if (result.isConfirmed) {
                        let itemObject = {item_code: item_code};
                        console.log('itemObject: ', itemObject);
                        let itemJSON = JSON.stringify(itemObject);
                        console.log('itemJSON: ', itemJSON);
                        $.ajax({
                            url: `${servletUrl}`,
                            type: "DELETE",
                            data: itemJSON,
                            headers: {"Content-Type": "application/json"},
                            success: (res) => {
                                console.log(JSON.stringify(res));
                                Swal.fire({width: '225px', position: 'center', icon: 'success', title: 'Deleted!', showConfirmButton: false, timer: 2000});
                                $("#item_btns>button[type='button']").eq(3).click();
                                loadItemData();
                            },
                            error: (err) => { console.error(err)}
                        });
                    }
                })
            } else { showError('This Code is not exist!');}
        } else { showError('Invalid item code format!');}
    } else { showError('Item Code can not be empty!');}
});

// reset item form
$("#item_btns>button[type='button']").eq(3).on("click", async () => {
    loadItemData();
    $("#description").val("");
    $("#unit_price").val("");
    $("#item_qty").val("");

    const urlWithParams = new URL(`${servletUrl}?item_code=get_next_code&search_term=get_next_code`);
    try {
        const response = await fetch(urlWithParams, {method: 'GET',});
        if (response.ok) {
            const data = await response.json();
            const nextItemCode = data.next_item_code;
            console.log("Next Item Code: ", nextItemCode);
            $("#item_code").val(nextItemCode);
        } else { console.error(`HTTP error! Status: ${response.status}`);}
    } catch (error) { console.error('Error:', error);}
});

// load all item details to the table
const loadItemData = () => {
    const urlWithParams = new URL(`${servletUrl}?item_code=get_all&search_term=get_all`);
    fetch(urlWithParams, { method: 'GET',})
        .then(response => {
            if (!response.ok) { throw new Error(`HTTP error! Status: ${response.status}`);}
            return response.json();
        })
        .then(data => {
            if (Array.isArray(data)) {
                console.log('Response item data:', data);
                $("#item_tbl_body").empty();
                data.forEach(item => {
                    let record = `<tr><td class="item_code">${item.item_code}</td><td class="description">${item.description}</td>
                    <td class="unit_price">${item.unit_price}</td><td class="qty_on_hand">${item.qty_on_hand}</td></tr>`;
                    $("#item_tbl_body").append(record);
                });
            } else { console.error('Error: Expected JSON array, but received: ', data);}
        })
    .catch(error => { console.error('Error:', error);});
};

// retrieve item by table click
$("#item_tbl_body, #item_search_tbl_body").on("click", "tr", function() {
    row_index = $(this).index();
    let item_id = $(this).find(".item_code").text();
    let description = $(this).find(".description").text();
    let unit_price = $(this).find(".unit_price").text();
    let qty_on_hand = $(this).find(".qty_on_hand").text();
    $("#item_code").val(item_id);
    $("#description").val(description);
    $("#unit_price").val(unit_price);
    $("#item_qty").val(qty_on_hand);
});

// search items by using item_code or description
$('#item_search_box').on('input', () => {
    let search_term = $('#item_search_box').val();
    if(search_term){
        $('#item_search_tbl_body').empty();
        const urlWithParams = new URL(`${servletUrl}?item_code=use_search_term&search_term=${search_term}`);
        fetch(urlWithParams, { method: 'GET', })
        .then(response => {
            if (!response.ok) { throw new Error(`HTTP error! Status: ${response.status}`);}
            return response.json();
        })
        .then(data => {
            if (Array.isArray(data)) {
                console.log('Response item data: ', data);
                data.forEach(item => {
                let record = `<tr><td class="item_code">${item.item_code}</td><td class="description">${item.description}</td>
                <td class="unit_price">${item.unit_price}</td><td class="qty_on_hand">${item.qty_on_hand}</td></tr>`;
                $("#item_search_tbl_body").append(record);
                });
            } else { console.error('Error: Expected JSON array, but received:', data);}
        })
        .catch(error => { console.error('Error:', error);});
    }else{ $('#item_search_tbl_body').empty(); }
});

// v1 concise & finalize