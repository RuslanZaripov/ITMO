use image::{ImageBuffer, Rgb};
use rand::Rng;

use crate::camera::Camera;
use crate::equation::Equation;
use crate::hittable::{Hittable, HittableList};
use crate::ray::Ray;
use crate::sphere::Sphere;
use crate::vec3::Vec3;

pub mod equation;
pub mod sphere;
pub mod hittable;
pub mod ray;
pub mod vec3;
pub mod material;
pub mod camera;

#[allow(unused_variables)]
fn main() {
    let ratio = 16.0 / 9.0;
    let width = 800;
    let height = (width as f64 / ratio) as u32;
    let samples_per_pixel = 100;

    let mut scene: HittableList = HittableList::new();
    scene.add(Sphere::new(Vec3::new(0.0, 0.0, -1.0), 0.5));
    scene.add(Sphere::new(Vec3::new(-2.0, 0.0, -3.0), 1.0));

    let camera = Camera::new();

    let mut img = image::ImageBuffer::new(width, height);

    render(&mut img, &scene, &camera, &samples_per_pixel);
    img.save("images/img.png").unwrap();
}

pub fn render(image: &mut ImageBuffer<Rgb<u8>, Vec<u8>>,
              scene: &HittableList,
              camera: &Camera,
              samples_per_pixel: &u32,
) {
    let (width, height) = image.dimensions();
    for (i, j, pixel) in image.enumerate_pixels_mut() {
        let mut color_pixel = Vec3::new(0.0, 0.0, 0.0);
        for _ in 0..*samples_per_pixel {
            let u = (i as f64 + get_random_double()) / (width - 1) as f64;
            let v = (j as f64 + get_random_double()) / (height - 1) as f64;
            let ray = camera.get_ray(u, v);
            color_pixel += cast_ray(&ray, scene);
        }
        *pixel = get_color(&color_pixel, samples_per_pixel);
    }
}

fn get_random_double() -> f64 {
    rand::thread_rng().gen_range(0.0..1.0)
}


fn get_color(color: &Vec3, samples_per_pixel: &u32) -> Rgb<u8> {
    let scale = 1.0 / *samples_per_pixel as f64;
    let r = color.x * scale;
    let g = color.y * scale;
    let b = color.z * scale;
    Rgb([(256.0 * clamp(r, 0.0, 0.999)) as u8,
        (256.0 * clamp(g, 0.0, 0.999)) as u8,
        (256.0 * clamp(b, 0.0, 0.999)) as u8])
}

fn clamp(x: f64, min: f64, max: f64) -> f64 {
    if x < min { return min; }
    if x > max { return max; }
    x
}

pub fn cast_ray(ray: &Ray, scene: &HittableList) -> Vec3 {
    match scene.hit(ray, 0.0, f64::MAX) {
        Some(rec) => {
            0.5 * (rec.normal + Vec3::new(1.0, 1.0, 1.0))
        }
        None => {
            let unit_dir = ray.dir.unit_vector();
            let t = 0.5 * (unit_dir.y + 1.0);
            (1.0 - t) * Vec3::new(1.0, 1.0, 1.0) + t * Vec3::new(0.5, 0.7, 1.0)
        }
    }
}
