package com.example.izin.ui;

import com.example.izin.Model.Leave;
import com.example.izin.Services.LeaveService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;


@PageTitle("Leave Management")
@Route(value = "leave-details/")
public class EmployeeDetailView extends VerticalLayout {

    private final LeaveService leaveService;
    private final Grid<Leave> grid = new Grid<>(Leave.class);

    public EmployeeDetailView(LeaveService leaveService) {
        this.leaveService = leaveService;

        setSizeFull();
        configureGrid();
        configureButtons();

        add(grid);
        updateGrid();
    }

    private void configureGrid() {
        grid.removeAllColumns();

        grid.addColumn(Leave::getId).setHeader("ID");
        grid.addColumn(Leave::getEmpId).setHeader("Employee ID");
        grid.addColumn(Leave::getLeaveReason).setHeader("Reason");
        grid.addColumn(Leave::getLeaveApprover).setHeader("Approver");
        grid.addColumn(Leave::getLeaveStart).setHeader("Start Date");
        grid.addColumn(Leave::getLeaveEnd).setHeader("End Date");

        grid.getColumns().forEach(column -> column.setAutoWidth(true));
    }

    private void updateGrid() {
        grid.setItems(leaveService.findAll());
    }

    private void configureButtons() {
        Button addButton = new Button("Add Leave", event -> showAddLeaveForm());
        add(addButton);
    }

    private void showAddLeaveForm() {
        Dialog dialog = new Dialog();

        TextField empIdField = new TextField("Employee ID");
        TextField reasonField = new TextField("Reason");
        TextField approverField = new TextField("Approver");
        TextField startDateField = new TextField("Start Date (YYYY-MM-DD)");
        TextField endDateField = new TextField("End Date (YYYY-MM-DD)");

        Button saveButton = new Button("Save", event -> {
            Leave leave = new Leave();
            leave.setEmpId(Long.parseLong(empIdField.getValue()));
            leave.setLeaveReason(reasonField.getValue());
            leave.setLeaveApprover(approverField.getValue());
            leave.setLeaveStart(LocalDate.parse(startDateField.getValue()));
            leave.setLeaveEnd(LocalDate.parse(endDateField.getValue()));

            leaveService.saveOrUpdate(leave);
            Notification.show("Leave added successfully!");
            updateGrid();
            dialog.close();
        });

        Button cancelButton = new Button("Cancel", event -> dialog.close());

        VerticalLayout formLayout = new VerticalLayout(empIdField, reasonField, approverField, startDateField, endDateField, saveButton, cancelButton);
        dialog.add(formLayout);
        dialog.open();
    }
}
