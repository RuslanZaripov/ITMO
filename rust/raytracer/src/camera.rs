use crate::{Ray, Vec3};
use crate::vec3::cross;

#[derive(Debug, Copy, Clone)]
pub struct Camera {
    pub origin: Vec3,
    pub upper_left_corner: Vec3,
    pub horizontal: Vec3,
    pub vertical: Vec3
}

impl Camera {
    pub fn new(look_from: Vec3, look_at: Vec3, vup: Vec3, vfov: f64, ratio: f64) -> Self {
        let theta = vfov.to_radians();
        let h = (theta / 2.0).tan();
        let viewport_height = 2.0 * h;
        let viewport_width = ratio * viewport_height;

        let w = (look_from - look_at).unit_vector();
        let u = cross(&vup, &w).unit_vector();
        let v = cross(&w, &u);

        let origin = look_from;
        let horizontal = viewport_width * u;
        let vertical = viewport_height * v;
        let upper_left_corner = origin - horizontal / 2.0 + vertical / 2.0 - w;

        Self { origin, upper_left_corner, horizontal, vertical }
    }

    pub fn get_ray(&self, t1: f64, t2: f64) -> Ray {
        Ray::new(self.origin, self.upper_left_corner + self.horizontal * t1 + -self.vertical * t2 - self.origin)
    }
}
