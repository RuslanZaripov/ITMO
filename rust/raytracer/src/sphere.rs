pub(crate) mod ray;

use image::{ImageBuffer, Rgb};
use crate::{Equation, Vec3};
use crate::sphere::ray::Ray;
use crate::sphere::ray::vec3::dot;

#[derive(Debug, Copy, Clone)]
pub struct Sphere {
    pub center: Vec3,
    pub r: f64,
}

impl Sphere {
    pub fn new(center: Vec3, radius: f64) -> Sphere {
        Sphere { center, r: radius }
    }

    pub fn render(&self, image: &mut ImageBuffer<Rgb<u8>, Vec<u8>>,
                  upper_left_corner: Vec3,
                  horizontal: Vec3,
                  vertical: Vec3,
                  origin: Vec3
    ) {
        let (width, height) = image.dimensions();
        for (i, j, pixel) in image.enumerate_pixels_mut() {
            let u = i as f64 / (width - 1) as f64;
            let v = j as f64 / (height - 1) as f64;
            let dir = upper_left_corner + horizontal * u + -vertical * v - origin;
            let ray = Ray::new(origin, dir);
            let color = self.cast_ray(&ray);
            *pixel = Rgb([
                (color.x * 255.999) as u8,
                (color.y * 255.999) as u8,
                (color.z * 255.999) as u8]);
        }
    }

    pub fn intersect(&self, ray: &Ray) -> bool {
        let oc = ray.origin - self.center;
        let eq = Equation::new(
            dot(&ray.dir, &ray.dir),
            2.0 * dot(&oc, &ray.dir),
            dot(&oc, &oc) - self.r * self.r,
        );
        eq.get_discriminant() > 0.0
        // return match eq.solve() {
        //     Some((t1, t2)) => {
        //         println!("t1: {}, t2: {}", t1, t2);
        //         t1
        //     },
        //     None => -1.0,
        // }
    }

    pub fn cast_ray(&self, ray: &Ray) -> Vec3 {
        if self.intersect(ray) {
            return Vec3::new(1.0, 0.0, 0.0);
        }
        let unit_dir = ray.dir.unit_vector();
        let t = 0.5 * (unit_dir.y + 1.0);
        (1.0 - t) * Vec3::new(1.0, 1.0, 1.0) + t * Vec3::new(0.5, 0.7, 1.0)
        // let t = self.intersect(ray);
        // if t > 0.0 {
        //     let p = ray.at(t as f64);
        //     let normal = (p - self.center) / self.r;
        //     return 0.5 * Vec3::new(normal.x + 1.0, normal.y + 1.0, normal.z + 1.0)
        // }
        // let unit_direction = ray.dir.unit_vector();
        // let t = 0.5 * (unit_direction.y + 1.0);
        // let color = (1.0 - t) * Vec3::new(1.0, 1.0, 1.0) + t * Vec3::new(0.5, 0.7, 1.0);
        // println!("color: {:?}", color);
        // color
    }
}
