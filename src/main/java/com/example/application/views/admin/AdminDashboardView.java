package com.example.application.views.admin;

import com.example.application.views.MainLayout;
import com.example.application.model.moderation.Report;
import com.example.application.service.moderation.ModerationService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "admin", layout = MainLayout.class)
@PageTitle("Panel Moderasi & Admin | Rewear")
@Menu(order = 4, icon = "line-awesome/svg/shield-alt-solid.svg", title = "Admin & Moderasi")
public class AdminDashboardView extends VerticalLayout {

    private final ModerationService moderationService;
    private final Grid<Report> reportGrid = new Grid<>(Report.class, false);

    public AdminDashboardView(ModerationService moderationService) {
        this.moderationService = moderationService;

        setSpacing(true);
        setPadding(true);
        setSizeFull();

        add(createHeader(), createReportGrid());
        refreshGrid();
    }

    private VerticalLayout createHeader() {
        H2 title = new H2("🛡️ Panel Admin & Moderasi");
        Paragraph description = new Paragraph("Verifikasi verifikasi sekolah dan proses keluhan/laporan pengguna.");
        VerticalLayout header = new VerticalLayout(title, description);
        header.setPadding(false);
        header.setSpacing(false);
        return header;
    }

    private Grid<Report> createReportGrid() {
        reportGrid.addColumn(r -> r.getReporter() != null ? r.getReporter().getFullName() : "-").setHeader("Pelapor");
        reportGrid.addColumn(r -> r.getType() != null ? r.getType().getValue() : "-").setHeader("Tipe Laporan");
        reportGrid.addColumn(Report::getReason).setHeader("Alasan");
        reportGrid.addColumn(r -> r.getStatus() != null ? r.getStatus().getValue() : "-").setHeader("Status");

        reportGrid.setSizeFull();
        return reportGrid;
    }

    private void refreshGrid() {
        reportGrid.setItems(moderationService.getPendingReports());
    }
}
