mod equation;
mod sphere;

use crate::sphere::Sphere;
use crate::equation::Equation;
use sphere::ray::vec3::Vec3;

use image::{ImageBuffer, Rgb};

fn main() {
    let ratio = 16.0 / 9.0;
    let width = 800;
    let height = (width as f64 / ratio) as u32;

    let viewport_height = 2.0;
    let viewport_width = ratio * viewport_height;
    let focal_length = 1.0;

    let origin = Vec3::new(0.0, 0.0, 0.0);
    let horizontal = Vec3::new(viewport_width, 0.0, 0.0);
    let vertical = Vec3::new(0.0, viewport_height, 0.0);
    let upper_left_corner = origin - horizontal / 2.0 + vertical / 2.0 - Vec3::new(0.0, 0.0, focal_length);

    let mut img = image::ImageBuffer::new(width, height);

    let sphere = Sphere::new(Vec3::new(0.0, 0.0, -1.0), 0.5);
    sphere.render(&mut img, upper_left_corner, horizontal, vertical, origin);
    img.save("images/img.png").unwrap();
}
