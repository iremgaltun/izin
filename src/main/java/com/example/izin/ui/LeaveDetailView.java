package com.example.izin.ui;

import com.example.izin.Model.EmpLeaveDTO;
import com.example.izin.Services.EmployeeService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.notification.Notification;
import java.time.LocalDate;
import java.util.List;

@Route("leave-details")
@PageTitle("İzin Detayları")
public class LeaveDetailView extends AppLayout {
    private final EmployeeService employeeService;
    private final Grid<EmpLeaveDTO> leaveGrid = new Grid<>(EmpLeaveDTO.class);

    public LeaveDetailView(EmployeeService employeeService) {
        this.employeeService = employeeService;

        createHeader();  // Navbar (Üst Kısım)
        createDrawer();  // Sidebar (Yan Menü)
        setContent(createContent());  // Ana İçerik
    }

    private void createHeader() {
        H1 title = new H1("İzin Yönetimi");
        title.setClassName("header");

        addToNavbar(new DrawerToggle(), title);
    }

    private void createDrawer() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        Tab employeesTab = new Tab(VaadinIcon.USERS.create(), new RouterLink("Çalışanlar", MainView.class));
        Tab leaveTab = new Tab(VaadinIcon.ARCHIVE.create(), new RouterLink("İzinler", LeaveDetailView.class));

        tabs.add(employeesTab, leaveTab);

        addToDrawer(tabs);
    }



    private VerticalLayout createContent() {
        HorizontalLayout filtersLayout = createDateFilterComponents();
        configureGrid();
        updateGrid(null, null);

        VerticalLayout contentLayout = new VerticalLayout(filtersLayout, leaveGrid);
        contentLayout.setSizeFull();
        return contentLayout;
    }

    private HorizontalLayout createDateFilterComponents() {
        ComboBox<String> timeFilterComboBox = new ComboBox<>("Tarih Filtresi");
        timeFilterComboBox.setItems("Son 3 Ay", "Son 6 Ay", "Tümü", "Özel Tarih Aralığı");
        timeFilterComboBox.setValue("Tümü");

        DatePicker startDatePicker = new DatePicker("Başlangıç Tarihi");
        DatePicker endDatePicker = new DatePicker("Bitiş Tarihi");
        startDatePicker.setVisible(false);
        endDatePicker.setVisible(false);

        timeFilterComboBox.addValueChangeListener(event -> {
            String selectedFilter = event.getValue();
            LocalDate filterDate = null;

            switch (selectedFilter) {
                case "Son 3 Ay" -> filterDate = LocalDate.now().minusMonths(3);
                case "Son 6 Ay" -> filterDate = LocalDate.now().minusMonths(6);
                case "Özel Tarih Aralığı" -> {
                    startDatePicker.setVisible(true);
                    endDatePicker.setVisible(true);
                    return;
                }
                default -> {
                    filterDate = null;
                }
            }
            startDatePicker.setVisible(false);
            endDatePicker.setVisible(false);
            updateGrid(filterDate, LocalDate.now());
        });

        startDatePicker.addValueChangeListener(event -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            updateGrid(startDate, endDate);
        });

        endDatePicker.addValueChangeListener(event -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            updateGrid(startDate, endDate);
        });

        HorizontalLayout layout = new HorizontalLayout(timeFilterComboBox, startDatePicker, endDatePicker);
        layout.setWidthFull();
        return layout;
    }

    private void configureGrid() {
        leaveGrid.removeAllColumns();
        leaveGrid.addColumn(EmpLeaveDTO::getEmployeeName).setHeader("Çalışan Adı");
        leaveGrid.addColumn(EmpLeaveDTO::getEmployeeLastname).setHeader("Çalışan Soyadı");
        leaveGrid.addColumn(EmpLeaveDTO::getLeaveReason).setHeader("İzin Nedeni");
        leaveGrid.addColumn(EmpLeaveDTO::getLeaveApprover).setHeader("İzin Veren");
        leaveGrid.addColumn(EmpLeaveDTO::getLeaveStart).setHeader("Başlangıç Tarihi").setSortable(true);
        leaveGrid.addColumn(EmpLeaveDTO::getLeaveEnd).setHeader("Bitiş Tarihi").setSortable(true);

        leaveGrid.getColumns().forEach(column -> column.setAutoWidth(true));
    }

    private void updateGrid(LocalDate filterDate, LocalDate specificDate) {
        List<EmpLeaveDTO> leaveDetails = employeeService.getAllEmployeeLeaveDetails();

        if (filterDate != null) {
            leaveDetails = leaveDetails.stream()
                    .filter(leave -> !leave.getLeaveStart().isBefore(filterDate))
                    .toList();
        }

        if (specificDate != null) {
            leaveDetails = leaveDetails.stream()
                    .filter(leave ->
                            (leave.getLeaveStart().isEqual(specificDate) || leave.getLeaveStart().isBefore(specificDate)) &&
                                    (leave.getLeaveEnd().isEqual(specificDate) || leave.getLeaveEnd().isAfter(specificDate))
                    ).toList();
        }

        leaveGrid.setItems(leaveDetails);

        if (leaveDetails.isEmpty()) {
            Notification.show("Belirtilen filtreye göre kayıt bulunamadı.", 3000, Notification.Position.MIDDLE);
        }
    }
}
