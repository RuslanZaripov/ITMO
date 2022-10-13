use crate::{Ray, Vec3};

#[derive(Debug, Copy, Clone)]
pub struct Camera {
    pub origin: Vec3,
    pub upper_left_corner: Vec3,
    pub horizontal: Vec3,
    pub vertical: Vec3
}

impl Camera {
    pub fn new(vfov: f64, ratio: f64) -> Self {
        let theta = vfov.to_radians();
        let h = (theta / 2.0).tan();
        let viewport_height = 2.0 * h;
        let viewport_width = ratio * viewport_height;

        let focal_length = 1.0;

        let origin = Vec3::new(0.0, 0.0, 0.0);
        let horizontal = Vec3::new(viewport_width, 0.0, 0.0);
        let vertical = Vec3::new(0.0, viewport_height, 0.0);
        let upper_left_corner = origin - horizontal / 2.0 + vertical / 2.0 - Vec3::new(0.0, 0.0, focal_length);

        Self { origin, upper_left_corner, horizontal, vertical }
    }

    pub fn get_ray(&self, u: f64, v: f64) -> Ray {
        Ray::new(self.origin, self.upper_left_corner + self.horizontal * u + -self.vertical * v - self.origin)
    }
}
