package ui;

public class Camera {

    private double x;
    private double y;
    private double zoom;

    public static final double MIN_ZOOM = 1.0;
    public static final double MAX_ZOOM = 3.0;

    private double worldWidth;
    private double worldHeight;
    private int viewportWidth;
    private int viewportHeight;
    private static final double CAMERA_MARGIN = 600;


    public Camera(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.zoom = 2.5;
    }

    public void setBounds(
            double worldWidth,
            double worldHeight,
            int viewportWidth,
            int viewportHeight
    ) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        clamp();
    }

    private void clamp() {
        if (viewportWidth <= 0 || viewportHeight <= 0) return;

        double viewW = viewportWidth / zoom;
        double viewH = viewportHeight / zoom;

        double minX = -CAMERA_MARGIN;
        double minY = -CAMERA_MARGIN;

        double maxX = worldWidth - viewW + CAMERA_MARGIN;
        double maxY = worldHeight - viewH + CAMERA_MARGIN;

        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));

    }

    public double getZoom() {
        return zoom;
    }

    public void move(double dx, double dy) {
        x += dx / zoom;
        y += dy / zoom;
        clamp();
    }

    public int worldToScreenX(double worldX) {
        return (int) ((worldX - x) * zoom);
    }

    public int worldToScreenY(double worldY) {
        return (int) ((worldY - y) * zoom);
    }

    public void zoomAt(double amount, int mouseX, int mouseY) {
        double worldXBefore = x + mouseX / zoom;
        double worldYBefore = y + mouseY / zoom;

        zoom += amount;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));

        x = worldXBefore - mouseX / zoom;
        y = worldYBefore - mouseY / zoom;

        clamp();
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        clamp();
    }

}
