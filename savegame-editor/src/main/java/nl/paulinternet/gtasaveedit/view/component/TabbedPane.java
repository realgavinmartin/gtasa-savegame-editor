package nl.paulinternet.gtasaveedit.view.component;

import nl.paulinternet.gtasaveedit.Settings;
import nl.paulinternet.gtasaveedit.model.SavegameModel;
import nl.paulinternet.gtasaveedit.view.Main;
import nl.paulinternet.gtasaveedit.view.menu.MenuBar;
import nl.paulinternet.gtasaveedit.view.pages.*;
import nl.paulinternet.gtasaveedit.view.pages.schools.PageSchools;
import nl.paulinternet.gtasaveedit.view.window.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabbedPane extends JTabbedPane {

    private static final Logger log = LoggerFactory.getLogger(TabbedPane.class);

    private Boolean loaded = Boolean.FALSE;
    private final PageAbout pageAbout;
    private List<Page> pages;

    public TabbedPane() {
        // Create pages
        pages = new ArrayList<>();

        pages.addAll(Arrays.asList(
                new PageGeneral(),
                new PageSkills(),
                new PageLocation(),
                new PageSchools(),
                new PageWeapons(),
                new PageGangWeapons(),
                new PageZones(),
                new PagePeds(),
                new PageClothes(),
                new PageCollectables(),
                new PageFix()));

        if (Settings.getGaragesEnabled() == Settings.YES) {
            pages.add(new PageGarages());
        }

        pageAbout = new PageAbout();
        if (!Main.MAC) {
            pages.add(pageAbout);
        }

        // Options should come last
        pages.add(new PageOptions());

        // Add tabs
        pages.forEach(p -> addTab(p.getTitle(), p.getComponent()));

        // Set the border
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Observe
        SavegameModel.gameLoaded.addHandler(this, "onGameLoaded");
        SavegameModel.gameClosed.addHandler(this, "onGameClosed");
        onGameClosed();

        addChangeListener(e -> {
            if (pageAbout.getComponent().equals(TabbedPane.this.getSelectedComponent()) && Settings.getSoundOnAboutPage() == Settings.YES) {
                pageAbout.play();
            } else {
                pageAbout.stop();
            }
        });
    }


    public void onGameLoaded() {
        if (!loaded) {
            removeAll();
            pages.forEach(p -> addTab(p.getTitle(), p.getComponent()));
            loaded = Boolean.TRUE;
            MenuBar menubar = (MenuBar) MainWindow.getInstance().getJMenuBar();
            if (menubar != null) {
                menubar.onSavegameStateChange(true);
            } else {
                log.error("Unable to get menuBar: " + MainWindow.getInstance().getJMenuBar());
            }
            MainWindow.getInstance().validate();
        }
    }

    public void onGameClosed() {
        removeAll();
        pages.forEach(p -> {
            if (p.isAlwaysVisible()) {
                addTab(p.getTitle(), p.getComponent());
            }
        });
        loaded = Boolean.FALSE;
        MenuBar menubar = (MenuBar) MainWindow.getInstance().getJMenuBar();
        if (menubar != null) {
            menubar.onSavegameStateChange(false);
        } else {
            log.error("Unable to get menuBar: " + MainWindow.getInstance().getJMenuBar());
        }
        MainWindow.getInstance().validate();
    }

    public void updateUI() {
        super.updateUI();
        if (!loaded && pages != null) {
            pages.forEach(p -> {
                if (!p.isAlwaysVisible()) {
                    SwingUtilities.updateComponentTreeUI(p.getComponent());
                }
            });
        }
    }

    public void onShowPreferences() {
        pages.forEach(p -> {
            if (p instanceof PageOptions) {
                setSelectedComponent(p.getComponent());
            }
        });
    }
}
