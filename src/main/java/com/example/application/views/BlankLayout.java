package com.example.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.RouterLayout;

/**
 * BlankLayout — Empty router layout without top navbar, used for standalone pages like Login.
 */
public class BlankLayout extends Div implements RouterLayout {

    public BlankLayout() {
        setSizeFull();
        getElement().getStyle()
            .set("margin", "0")
            .set("padding", "0")
            .set("width", "100%")
            .set("height", "100vh");
    }
}
